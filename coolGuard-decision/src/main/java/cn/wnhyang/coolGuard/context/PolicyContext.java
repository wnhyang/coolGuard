package cn.wnhyang.coolGuard.context;

import cn.hutool.core.collection.CollUtil;
import cn.wnhyang.coolGuard.constant.DisposalConstant;
import cn.wnhyang.coolGuard.constant.PolicyMode;
import cn.wnhyang.coolGuard.constant.RuleStatus;
import cn.wnhyang.coolGuard.entity.Action;
import cn.wnhyang.coolGuard.entity.Cond;
import cn.wnhyang.coolGuard.entity.Th;
import cn.wnhyang.coolGuard.vo.result.PolicyResult;
import cn.wnhyang.coolGuard.vo.result.PolicySetResult;
import cn.wnhyang.coolGuard.vo.result.RuleResult;
import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wnhyang
 * @date 2024/4/3
 **/
public class PolicyContext {

    /**
     * 处置方式集合
     */
    private final Map<String, DisposalCtx> disposalMap = new ConcurrentHashMap<>();

    /**
     * 策略集
     */
    private PolicySetCtx policySet;

    /**
     * 初始化
     *
     * @param disposalCtxList 处置方式集合
     * @param policySet       策略集
     */
    public void init(List<DisposalCtx> disposalCtxList, PolicySetCtx policySet) {
        for (DisposalCtx disposalCtx : disposalCtxList) {
            disposalMap.put(disposalCtx.getCode(), disposalCtx);
        }
        this.policySet = policySet;
    }

    /**
     * 策略集合
     */
    private final Map<String, PolicyCtx> policyMap = new ConcurrentHashMap<>();

    /**
     * 添加策略
     *
     * @param policyCode 策略code
     * @param policy     策略
     */
    public void addPolicy(String policyCode, PolicyCtx policy) {
        policyMap.put(policyCode, policy);
    }

    /**
     * 获取策略
     *
     * @param policyCode 策略code
     * @return 策略
     */
    public PolicyCtx getPolicy(String policyCode) {
        return policyMap.get(policyCode);
    }

    /**
     * 规则集合
     */
    private final Map<String, List<RuleCtx>> ruleListMap = new ConcurrentHashMap<>();

    /**
     * 添加规则集合
     *
     * @param policyCode 策略code
     * @param ruleList   规则列表
     */
    public void addRuleList(String policyCode, List<RuleCtx> ruleList) {
        ruleListMap.put(policyCode, ruleList);
    }

    /**
     * 获取规则
     *
     * @param policyCode 策略code
     * @param index      规则索引
     * @return 规则
     */
    public RuleCtx getRule(String policyCode, int index) {
        return ruleListMap.get(policyCode).get(index);
    }

    /**
     * 命中规则集合
     */
    private final Map<String, List<RuleCtx>> hitRuleListMap = new ConcurrentHashMap<>();

    /**
     * 添加命中规则
     *
     * @param policyCode 策略code
     * @param rule       规则
     */
    public void addHitRuleVO(String policyCode, RuleCtx rule) {
        if (!hitRuleListMap.containsKey(policyCode)) {
            hitRuleListMap.put(policyCode, CollUtil.newArrayList());
        }
        hitRuleListMap.get(policyCode).add(rule);
    }

    /**
     * 是否命中风险规则
     *
     * @param policyCode 策略code
     * @return true/false
     */
    public boolean isHitRisk(String policyCode) {
        if (CollUtil.isNotEmpty(hitRuleListMap.get(policyCode))) {
            for (RuleCtx ruleCtx : hitRuleListMap.get(policyCode)) {
                if (!DisposalConstant.PASS_CODE.equals(ruleCtx.getDisposalCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 命中模拟规则集合
     */
    private final Map<String, List<RuleCtx>> hitMockRuleListMap = new ConcurrentHashMap<>();

    /**
     * 添加命中模拟规则
     *
     * @param policyCode 策略code
     * @param rule       规则
     */
    public void addHitMockRuleVO(String policyCode, RuleCtx rule) {
        if (!hitMockRuleListMap.containsKey(policyCode)) {
            hitMockRuleListMap.put(policyCode, CollUtil.newArrayList());
        }
        hitMockRuleListMap.get(policyCode).add(rule);
    }

    /**
     * 转策略集结果
     *
     * @return 策略集结果
     */
    public PolicySetResult convert() {
        PolicySetResult policySetResult = new PolicySetResult(policySet.getName(), policySet.getCode(), policySet.getChain(), policySet.getVersion());

        for (Map.Entry<String, PolicyCtx> entry : policyMap.entrySet()) {
            PolicyCtx policy = entry.getValue();
            PolicyResult policyResult = new PolicyResult(policy.getName(), policy.getCode(), policy.getMode());

            // 最坏
            String maxDisposalCode = DisposalConstant.PASS_CODE;
            int maxGrade = Integer.MIN_VALUE;
            // 投票
            Map<String, Integer> votes = new HashMap<>();
            // 权重
            double weight = 0.0;
            List<RuleCtx> ruleList = hitRuleListMap.get(policy.getCode());
            if (CollUtil.isNotEmpty(ruleList)) {
                for (RuleCtx rule : ruleList) {
                    if (PolicyMode.VOTE.equals(policy.getMode())) {
                        // 投票
                        votes.put(rule.getDisposalCode(), votes.getOrDefault(rule.getDisposalCode(), 0) + 1);
                    } else if (PolicyMode.WEIGHT.equals(policy.getMode())) {
                        // 权重
                        weight += rule.getExpressValue();
                    }

                    RuleResult ruleResult = new RuleResult(rule.getName(), rule.getCode(), rule.getExpress());

                    // 最坏和顺序
                    DisposalCtx disposal = disposalMap.get(rule.getDisposalCode());
                    if (null != disposal) {
                        ruleResult.setDisposalName(disposal.getName());
                        ruleResult.setDisposalCode(disposal.getCode());
                        if (disposal.getGrade() > maxGrade) {
                            maxGrade = disposal.getGrade();
                            maxDisposalCode = disposal.getCode();
                        }
                    }
                    // 模拟/正式规则区分开
                    if (RuleStatus.MOCK.equals(rule.getStatus())) {
                        policyResult.addMockRuleResult(ruleResult);
                    } else {
                        policyResult.addRuleResult(ruleResult);
                    }
                }
            }
            if (PolicyMode.VOTE.equals(policy.getMode())) {
                String maxVoteDisposalCode = DisposalConstant.PASS_CODE;
                int maxVoteCount = Integer.MIN_VALUE;
                for (Map.Entry<String, Integer> entry1 : votes.entrySet()) {
                    if (entry1.getValue() > maxVoteCount) {
                        maxVoteCount = entry1.getValue();
                        maxVoteDisposalCode = entry1.getKey();
                    }
                }
                policyResult.setDisposalName(disposalMap.get(maxVoteDisposalCode).getName());
                policyResult.setDisposalCode(maxVoteDisposalCode);
            } else if (PolicyMode.WEIGHT.equals(policy.getMode())) {
                List<Th> thList = policy.getThList();
                // 排序
                thList.sort(Comparator.comparing(Th::getScore));
                for (Th th : thList) {
                    if (weight <= th.getScore()) {
                        policyResult.setDisposalName(disposalMap.get(th.getCode()).getName());
                        policyResult.setDisposalCode(th.getCode());
                        break;
                    }
                }
            } else {
                policyResult.setDisposalName(disposalMap.get(maxDisposalCode).getName());
                policyResult.setDisposalCode(maxDisposalCode);
            }
            policySetResult.addPolicyResult(policyResult);
        }
        // TODO 入度大于1？考虑投票、加权平均等方法：不考虑
        policySetResult.setDisposalName(DisposalConstant.PASS_NAME);
        policySetResult.setDisposalCode(DisposalConstant.PASS_CODE);

        return policySetResult;
    }

    @Data
    public static class PolicySetCtx {

        /**
         * 主键
         */
        private Long id;

        /**
         * app名
         */
        private String appName;

        /**
         * 策略集编码
         */
        private String code;

        /**
         * 策略集名
         */
        private String name;

        /**
         * 描述
         */
        private String description;

        /**
         * 策略集链
         */
        private String chain;

        /**
         * 发布
         */
        private Boolean publish;

        /**
         * 版本
         */
        private Integer version;
    }

    @Data
    public static class PolicyCtx {

        /**
         * 主键
         */
        private Long id;

        /**
         * 策略集id
         */
        private String policySetCode;

        /**
         * 策略编码
         */
        private String code;

        /**
         * 策略名
         */
        private String name;

        /**
         * 策略模式
         */
        private String mode;

        /**
         * 策略阈值
         */
        private List<Th> thList;

        /**
         * 描述
         */
        private String description;
    }

    @Data
    public static class RuleCtx {

        /**
         * 主键
         */
        private Long id;

        /**
         * 策略编码
         */
        private String policyCode;

        /**
         * 规则编码
         */
        private String code;

        /**
         * 规则名
         */
        private String name;

        /**
         * 处理编码
         */
        private String disposalCode;

        /**
         * 表达式
         */
        private String express;

        /**
         * 表达式值
         */
        private Double expressValue;

        /**
         * 状态
         */
        private String status;

        /**
         * 排序
         */
        private Integer sort;

        /**
         * 描述
         */
        private String description;

        /**
         * 条件
         */
        private Cond cond;

        /**
         * true执行
         */
        private Action ruleTrue;

        /**
         * false执行
         */
        private Action ruleFalse;
    }

    @Data
    public static class DisposalCtx {

        /**
         * 主键
         */
        private Long id;

        /**
         * 处置编码
         */
        private String code;

        /**
         * 处置名
         */
        private String name;

        /**
         * 等级
         */
        private Integer grade;

        /**
         * 颜色
         */
        private String color;

        /**
         * 是否为标准
         */
        private Boolean standard;

        /**
         * 描述
         */
        private String description;
    }

}