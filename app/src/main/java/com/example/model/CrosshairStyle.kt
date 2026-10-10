package com.example.model

/**
 * 准星样式类型枚举
 */
enum class CrosshairStyle(
    val title: String,
    val description: String
) {
    CLASSIC_CROSS("经典十字", "经典四向十字准星，适合大多数射击游戏"),
    CENTER_DOT("实心圆点", "单点精准瞄准，遮挡面积小，适合狙击与点射"),
    HALO_CIRCLE("空心光环", "圆形圆环准星，适合喷子与近战腰射"),
    CIRCLE_DOT("圆环瞄点", "外圆环加中心瞄点，锁定目标迅速"),
    TACTICAL_T("战术T型", "去掉上十字线，开阔上方视野，爆头利器"),
    CORNER_BOX("缺角方框", "四角战术准星框，战术感强烈"),
    TRI_CHEVRON("三叉倒Y", "三叉刻度线，视野开阔且导向清晰"),
    ROTATED_X("交叉X型", "斜向交叉准星，风格独特易识别"),
    SNIPER_TICKS("狙击刻度", "十字带下垂密位刻度线，适合远程狙击"),
    DUAL_RING("同心双环", "双重同心圆环，超大视界快速对准"),
    DIAMOND("战术菱形", "菱形边框准星，极速捕捉中心"),
    HOLLOW_BOX("空心准星框", "方框准星带中心间距，适合近战腰射"),
    APEX_CHEVRON("突击倒V", "尖头三角准星，精准箭头指引"),
    MICRO_PIN("极细微点", "超小微型瞄准点，适合极限高倍镜精准点杀")
}
