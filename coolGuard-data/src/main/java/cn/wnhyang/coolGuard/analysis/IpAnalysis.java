package cn.wnhyang.coolGuard.analysis;

import cn.wnhyang.coolGuard.analysis.ip.Ip2Region;

/**
 * @author wnhyang
 * @date 2024/5/31
 **/
public interface IpAnalysis {

    default void init() {

    }

    /**
     * 获取ip2region
     *
     * @param ip ip
     * @return ip2region
     */
    Ip2Region analysis(String ip);
}