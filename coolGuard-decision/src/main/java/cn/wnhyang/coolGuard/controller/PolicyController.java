package cn.wnhyang.coolGuard.controller;

import cn.wnhyang.coolGuard.entity.LabelValue;
import cn.wnhyang.coolGuard.pojo.CommonResult;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.service.PolicyService;
import cn.wnhyang.coolGuard.vo.PolicyVO;
import cn.wnhyang.coolGuard.vo.base.VersionSubmitVO;
import cn.wnhyang.coolGuard.vo.create.PolicyCreateVO;
import cn.wnhyang.coolGuard.vo.page.PolicyPageVO;
import cn.wnhyang.coolGuard.vo.update.PolicyUpdateVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.wnhyang.coolGuard.pojo.CommonResult.success;

/**
 * 策略
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    /**
     * 新增
     *
     * @param createVO 创建VO
     * @return id
     */
    @PostMapping
    public CommonResult<Long> createPolicy(@RequestBody @Valid PolicyCreateVO createVO) {
        return success(policyService.createPolicy(createVO));
    }

    /**
     * 更新
     *
     * @param updateVO 更新VO
     * @return true/false
     */
    @PutMapping
    public CommonResult<Boolean> updatePolicy(@RequestBody @Valid PolicyUpdateVO updateVO) {
        policyService.updatePolicy(updateVO);
        return success(true);
    }

    /**
     * 删除
     *
     * @param id id
     * @return true/false
     */
    @DeleteMapping
    public CommonResult<Boolean> deletePolicy(@RequestParam("id") Long id) {
        policyService.deletePolicy(id);
        return success(true);
    }

    /**
     * 查询单个
     *
     * @param id id
     * @return vo
     */
    @GetMapping
    public CommonResult<PolicyVO> getPolicy(@RequestParam("id") Long id) {
        return success(policyService.getPolicy(id));
    }

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    @GetMapping("/page")
    public CommonResult<PageResult<PolicyVO>> pagePolicy(@Valid PolicyPageVO pageVO) {
        return success(policyService.pagePolicy(pageVO));
    }

    /**
     * 提交
     *
     * @param submitVO submitVO
     * @return true/false
     */
    @PostMapping("/submit")
    public CommonResult<Boolean> submit(@RequestBody @Valid VersionSubmitVO submitVO) {
        policyService.submit(submitVO);
        return success(true);
    }

    /**
     * 根据策略集编码查询列表
     *
     * @param setCode 策略集编码
     * @return list
     */
    @GetMapping("/list")
    public CommonResult<List<PolicyVO>> listByPolicySetCode(@RequestParam("setCode") String setCode) {
        return success(policyService.listByPolicySetCode(setCode));
    }

    /**
     * 获取策略lv列表
     *
     * @return list
     */
    @GetMapping("/lvList")
    public CommonResult<List<LabelValue>> getLabelValueList() {
        return success(policyService.getLabelValueList());
    }
}
