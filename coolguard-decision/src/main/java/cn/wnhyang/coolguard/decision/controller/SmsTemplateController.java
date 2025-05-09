package cn.wnhyang.coolguard.decision.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.wnhyang.coolguard.common.entity.LabelValue;
import cn.wnhyang.coolguard.common.pojo.CommonResult;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.common.util.ExcelUtil;
import cn.wnhyang.coolguard.decision.convert.SmsTemplateConvert;
import cn.wnhyang.coolguard.decision.service.SmsTemplateService;
import cn.wnhyang.coolguard.decision.vo.SmsTemplateVO;
import cn.wnhyang.coolguard.decision.vo.create.SmsTemplateCreateVO;
import cn.wnhyang.coolguard.decision.vo.page.SmsTemplatePageVO;
import cn.wnhyang.coolguard.decision.vo.update.SmsTemplateUpdateVO;
import cn.wnhyang.coolguard.log.annotation.OperateLog;
import cn.wnhyang.coolguard.log.enums.OperateType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.wnhyang.coolguard.common.pojo.CommonResult.success;


/**
 * 消息模版表
 *
 * @author wnhyang
 * @since 2024/12/08
 */
@Slf4j
@RestController
@RequestMapping("/smsTemplate")
@RequiredArgsConstructor
public class SmsTemplateController {

    private final SmsTemplateService smsTemplateService;

    /**
     * 新增
     *
     * @param createVO 创建VO
     * @return id
     */
    @PostMapping
    @SaCheckPermission("decision:smsTemplate:create")
    @OperateLog(module = "后台-消息模版", name = "新增消息模版", type = OperateType.CREATE)
    public CommonResult<Long> create(@RequestBody @Valid SmsTemplateCreateVO createVO) {
        return success(smsTemplateService.create(createVO));
    }

    /**
     * 更新
     *
     * @param updateVO 更新VO
     * @return true/false
     */
    @PutMapping
    @SaCheckPermission("decision:smsTemplate:update")
    @OperateLog(module = "后台-消息模版", name = "更新消息模版", type = OperateType.UPDATE)
    public CommonResult<Boolean> update(@RequestBody @Valid SmsTemplateUpdateVO updateVO) {
        smsTemplateService.update(updateVO);
        return success(true);
    }

    /**
     * 删除
     *
     * @param id id
     * @return true/false
     */
    @DeleteMapping
    @SaCheckPermission("decision:smsTemplate:delete")
    @OperateLog(module = "后台-消息模版", name = "删除消息模版", type = OperateType.DELETE)
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        smsTemplateService.delete(id);
        return success(true);
    }

    /**
     * 查询单个
     *
     * @param id id
     * @return vo
     */
    @GetMapping
    @SaCheckLogin
    public CommonResult<SmsTemplateVO> get(@RequestParam("id") Long id) {
        return success(SmsTemplateConvert.INSTANCE.convert(smsTemplateService.get(id)));
    }

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    @GetMapping("/page")
    @SaCheckLogin
    public CommonResult<PageResult<SmsTemplateVO>> page(@Valid SmsTemplatePageVO pageVO) {
        return success(SmsTemplateConvert.INSTANCE.convert(smsTemplateService.page(pageVO)));
    }

    /**
     * 导出
     *
     * @param pageVO   导出VO
     * @param response response
     * @throws IOException IO异常
     */
    @GetMapping("/export")
    @SaCheckPermission("decision:smsTemplate:export")
    public void exportExcel(@Valid SmsTemplatePageVO pageVO, HttpServletResponse response) throws IOException {
        // 输出 Excel
        ExcelUtil.write(response, "SmsTemplateVO.xls", "数据", SmsTemplateVO.class, SmsTemplateConvert.INSTANCE.convert(smsTemplateService.page(pageVO)).getList());
    }

    /**
     * 导入
     *
     * @param file 文件
     * @return 结果
     * @throws IOException IO异常
     */
    @PostMapping("/import")
    @SaCheckPermission("decision:smsTemplate:import")
    public CommonResult<Boolean> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<SmsTemplateVO> read = ExcelUtil.read(file, SmsTemplateVO.class);
        // do something
        return success(true);
    }

    /**
     * 获取lvList
     *
     * @return lvList
     */
    @GetMapping("/lvList")
    @SaCheckLogin
    public CommonResult<List<LabelValue>> getLabelValueList() {
        return success(smsTemplateService.getLabelValueList());
    }
}
