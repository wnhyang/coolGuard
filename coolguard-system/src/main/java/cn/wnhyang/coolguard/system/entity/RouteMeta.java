package cn.wnhyang.coolguard.system.entity;

import cn.wnhyang.coolguard.common.entity.LabelValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author wnhyang
 * @date 2025/1/4
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteMeta {

    /**
     * 激活图标（菜单）
     */
    private String activeIcon;

    /**
     * 当前激活的菜单，有时候不想激活现有菜单，需要激活父级菜单时使用
     */
    private String activePath;

    /**
     * 是否固定标签页
     *
     * @default false
     */
    private Boolean affixTab;

    /**
     * 固定标签页的顺序
     *
     * @default 0
     */
    private Integer affixTabOrder;

    /**
     * 需要特定的角色标识才可以访问
     *
     * @default []
     */
    private List<String> authority;

    /**
     * 徽标
     */
    private String badge;

    /**
     * 徽标类型
     */
    private String badgeType;

    /**
     * 徽标颜色
     */
    private String badgeVariants;

    /**
     * 当前路由的子级在菜单中不展现
     *
     * @default false
     */
    private Boolean hideChildrenInMenu;

    /**
     * 当前路由在面包屑中不展现
     *
     * @default false
     */
    private Boolean hideInBreadcrumb;

    /**
     * 当前路由在菜单中不展现
     *
     * @default false
     */
    private Boolean hideInMenu;

    /**
     * 当前路由在标签页不展现
     *
     * @default false
     */
    private Boolean hideInTab;

    /**
     * 图标（菜单/tab）
     */
    private String icon;

    /**
     * iframe 地址
     */
    private String iframeSrc;

    /**
     * 忽略权限，直接可以访问
     *
     * @default false
     */
    private Boolean ignoreAccess;

    /**
     * 开启KeepAlive缓存
     */
    private Boolean keepAlive;

    /**
     * 外链-跳转路径
     */
    private String link;

    /**
     * 路由是否已经加载过
     */
    private Boolean loaded;

    /**
     * 标签页最大打开数量
     *
     * @default false
     */
    private Integer maxNumOfOpenTab;

    /**
     * 菜单可以看到，但是访问会被重定向到403
     */
    private Boolean menuVisibleWithForbidden;

    /**
     * 在新窗口打开
     */
    private Boolean openInNewWindow;

    /**
     * 用于路由->菜单排序
     */
    private Integer order;

    /**
     * 菜单所携带的参数
     */
    private Map<String, Object> query;

    /**
     * 标题名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String title;

    public static List<LabelValue> LABEL_VALUE_LIST = List.of(
            new LabelValue("激活图标（菜单）", "activeIcon"),
            new LabelValue("当前激活的菜单，有时候不想激活现有菜单，需要激活父级菜单时使用", "activePath"),
            new LabelValue("是否固定标签页", "affixTab"),
            new LabelValue("固定标签页的顺序", "affixTabOrder"),
            new LabelValue("需要特定的角色标识才可以访问", "authority"),
            new LabelValue("徽标", "badge"),
            new LabelValue("徽标类型", "badgeType"),
            new LabelValue("徽标颜色", "badgeVariants"),
            new LabelValue("当前路由的子级在菜单中不展现", "hideChildrenInMenu"),
            new LabelValue("当前路由在面包屑中不展现", "hideInBreadcrumb"),
            new LabelValue("当前路由在菜单中不展现", "hideInMenu"),
            new LabelValue("当前路由在标签页不展现", "hideInTab"),
            new LabelValue("图标（菜单/tab）", "icon"),
            new LabelValue("iframe 地址", "iframeSrc"),
            new LabelValue("忽略权限，直接可以访问", "ignoreAccess"),
            new LabelValue("开启KeepAlive缓存", "keepAlive"),
            new LabelValue("外链-跳转路径", "link"),
            new LabelValue("路由是否已经加载过", "loaded"),
            new LabelValue("标签页最大打开数量", "maxNumOfOpenTab"),
            new LabelValue("菜单可以看到，但是访问会被重定向到403", "menuVisibleWithForbidden"),
            new LabelValue("在新窗口打开", "openInNewWindow"),
            new LabelValue("用于路由->菜单排序", "order"),
            new LabelValue("菜单所携带的参数", "query"),
            new LabelValue("标题名称", "title"));
}
