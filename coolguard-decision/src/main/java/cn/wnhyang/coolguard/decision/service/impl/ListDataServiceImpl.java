package cn.wnhyang.coolguard.decision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.constant.ListDataSource;
import cn.wnhyang.coolguard.decision.context.DecisionContextHolder;
import cn.wnhyang.coolguard.decision.context.FieldContext;
import cn.wnhyang.coolguard.decision.convert.ListDataConvert;
import cn.wnhyang.coolguard.decision.entity.Action;
import cn.wnhyang.coolguard.decision.entity.ListData;
import cn.wnhyang.coolguard.decision.entity.ListSet;
import cn.wnhyang.coolguard.decision.mapper.ListDataMapper;
import cn.wnhyang.coolguard.decision.mapper.ListSetMapper;
import cn.wnhyang.coolguard.decision.service.ListDataService;
import cn.wnhyang.coolguard.decision.vo.create.ListDataCreateVO;
import cn.wnhyang.coolguard.decision.vo.page.ListDataPageVO;
import cn.wnhyang.coolguard.decision.vo.update.ListDataUpdateVO;
import cn.wnhyang.coolguard.redis.constant.RedisKey;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 名单数据表 服务实现类
 *
 * @author wnhyang
 * @since 2024/05/28
 */
@Slf4j
@Service
@LiteflowComponent
@RequiredArgsConstructor
public class ListDataServiceImpl implements ListDataService {

    private final ListDataMapper listDataMapper;

    private final ListSetMapper listSetMapper;

    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ListDataCreateVO createVO) {
        ListData listData = ListDataConvert.INSTANCE.convert(createVO);
        listDataMapper.insert(listData);
        return listData.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ListDataUpdateVO updateVO) {
        ListData listData = ListDataConvert.INSTANCE.convert(updateVO);
        listDataMapper.updateById(listData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        listDataMapper.deleteById(id);
    }

    @Override
    public ListData get(Long id) {
        return listDataMapper.selectById(id);
    }

    @Override
    public PageResult<ListData> page(ListDataPageVO pageVO) {
        return listDataMapper.selectPage(pageVO);
    }

    @Override
    public List<String> getListData(String setCode) {
        RList<String> values = redissonClient.getList(RedisKey.LIST_DATA + RedisKey.VALUES);
        // 使用分布式锁
        RLock lock = redissonClient.getLock(RedisKey.LIST_DATA + RedisKey.LOCK + setCode);
        try {
            // 尝试获取锁，最多等待10秒
            lock.lock(10, TimeUnit.SECONDS);

            if (!values.isEmpty()) {
                return values.readAll();
            }

            List<String> listDataList = listDataMapper.selectListBySetCode(setCode);
            if (!listDataList.isEmpty()) {
                values.addAll(listDataList);
                values.expire(Duration.ofMinutes(30));
            }
            return listDataList;
        } catch (Exception e) {
            log.error("Failed to process getListData for setCode: {}", setCode, e);
            // 根据实际情况决定是否需要抛出自定义异常或返回空列表等
            return Collections.emptyList();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();  // 释放锁
            }
        }
    }

    @Override
    public boolean hasListData(String setCode, String value) {
        ListSet listSet = listSetMapper.selectByCode(setCode);
        if (listSet != null) {
            // 两种方式，1、查询名单集id和名单数据作为一次查询；2、查询名单集所有名单数据做集合，再判断是否存在名单数据
            // 区别：1做缓存会是很多个，2做缓存更值得一些
            List<String> listData = getListData(setCode);
            if (CollUtil.isNotEmpty(listData)) {
                return listData.contains(value);
            }
        }
        return false;
    }

    @Override
    public void addListData(List<Action.AddList> addLists) {
        if (CollUtil.isEmpty(addLists)) {
            return;
        }
        FieldContext fieldContext = DecisionContextHolder.getFieldContext();
        // TODO 完善
        log.info("addListData");
        for (Action.AddList addList : addLists) {
            log.info("addListData:{}", addList);
            ListData listData = new ListData();
            listData.setListSetCode(addList.getListSetCode());
            listData.setValue(fieldContext.getData2String(addList.getFieldCode()));
            listData.setSource(ListDataSource.RULE);
            listDataMapper.insert(listData);
        }
    }

}
