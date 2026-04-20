export type AuthPreviewThemeId =
  | 'luxe'
  | 'luxeBlue'
  | 'playful'
  | 'minimal'
  | 'mallGlass'
  | 'promoDeck'
  | 'promoClean'
  | 'trustMart'
  | 'fastPass'
  | 'memberPerks'
  | 'promoTrustBlend'
  | 'pureMinimal'
  | 'homeShowcase'
  | 'categoryGlass'
  | 'cartReturn'
export type AuthPreviewMode = 'login' | 'register'

export interface AuthPreviewMetric {
  label: string
  value: string
}

export interface AuthPreviewHighlight {
  title: string
  description: string
}

export interface AuthPreviewTheme {
  id: AuthPreviewThemeId
  name: string
  tag: string
  summary: string
  heroEyebrow: string
  heroTitle: string
  heroSubtitle: string
  heroNote: string
  formBadge: string
  loginTitle: string
  loginDescription: string
  registerTitle: string
  registerDescription: string
  bullets?: string[]
  metrics: AuthPreviewMetric[]
  highlights: AuthPreviewHighlight[]
  variables: Record<string, string>
}

export const authPreviewThemes: AuthPreviewTheme[] = [
  {
    id: 'luxe',
    name: '样例 A / 轻奢电商',
    tag: '成熟消费品牌感',
    summary: '米白、暖金、深墨，强调品质感和信任感，最适合商城主入口。',
    heroEyebrow: 'YU-MALL SIGNATURE ACCESS',
    heroTitle: '把登录入口做成品牌橱窗，而不是普通表单。',
    heroSubtitle:
      '左侧用质感海报讲清商城气质，右侧表单保持克制和高级，适合长期使用，不容易看腻。',
    heroNote: '适合想要“看起来像正经品牌”的用户端。',
    formBadge: 'Warm Luxe',
    loginTitle: '欢迎回到 YU-Mall',
    loginDescription: '购物、权益、订单，一次登录继续你的上次浏览。',
    registerTitle: '注册你的 YU-Mall 账号',
    registerDescription: '三步完成注册，头像改为可选项，主流程更轻。',
    metrics: [
      { label: '品牌信任', value: '97%' },
      { label: '视觉气质', value: 'Premium' },
      { label: '适配周期', value: '长期耐看' }
    ],
    highlights: [
      { title: '暖金微光', description: '用暖色高光代替廉价的大渐变，整体更克制。' },
      { title: '双栏主视觉', description: '桌面端像品牌落地页，移动端自动折叠回单栏。' },
      { title: '核心动作清晰', description: '登录与注册按钮在视觉上始终是第一焦点。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #f6efe6 0%, #f2dfc6 48%, #d8c2a3 100%)',
      '--auth-surface': 'rgba(255, 251, 245, 0.82)',
      '--auth-border': 'rgba(130, 92, 35, 0.18)',
      '--auth-text': '#2f2419',
      '--auth-muted': '#7c6755',
      '--auth-accent': '#b77928',
      '--auth-accent-soft': 'rgba(183, 121, 40, 0.12)',
      '--auth-hero': '#2f2419',
      '--auth-hero-soft': 'rgba(47, 36, 25, 0.08)',
      '--auth-shadow': '0 28px 60px rgba(79, 52, 20, 0.18)',
      '--auth-card-radius': '28px',
      '--auth-button-radius': '999px',
      '--auth-display-font': '"Source Han Serif SC", "Songti SC", serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'luxeBlue',
    name: '样例 A+ / 蓝调轻奢',
    tag: '稳重高端品牌感',
    summary: '保留样例 A 的高级版式，用深海蓝替代暖金，整体更冷静、更耐看。',
    heroEyebrow: 'YU-MALL SIGNATURE ACCESS',
    heroTitle: '把轻奢商城的质感收进更稳的蓝色体系里。',
    heroSubtitle:
      '仍然是双栏品牌页逻辑，但把视觉情绪从暖金切到深蓝，更像高端会员、电商旗舰和品质消费品牌。',
    heroNote: '适合想要高级感，但不想走暖金路线的商城入口。',
    formBadge: 'Azure Luxe',
    loginTitle: '欢迎回到 YU-Mall',
    loginDescription: '在更冷静的蓝色秩序里，保留品牌感、信任感和主按钮焦点。',
    registerTitle: '注册你的 YU-Mall 账号',
    registerDescription: '延续样例 A 的成熟感，但整体更清爽，更适合长期线上运营。',
    metrics: [
      { label: '品牌信任', value: '98%' },
      { label: '气质倾向', value: 'Calm Luxe' },
      { label: '长期适配', value: '更稳' }
    ],
    highlights: [
      { title: '深海蓝替金色', description: '保留高级结构，把情绪从温暖切到克制与专业。' },
      { title: '品牌页逻辑不变', description: '左侧品牌叙事，右侧表单聚焦，仍然适合登录入口。' },
      { title: '适合高信任场景', description: '更像会员体系、品质消费、旗舰店首页入口。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #eef4fb 0%, #d8e6f4 45%, #97b2d2 100%)',
      '--auth-surface': 'rgba(248, 251, 255, 0.82)',
      '--auth-border': 'rgba(41, 88, 137, 0.18)',
      '--auth-text': '#1f2c3c',
      '--auth-muted': '#587089',
      '--auth-accent': '#2d6fb3',
      '--auth-accent-soft': 'rgba(45, 111, 179, 0.12)',
      '--auth-hero': '#1c2f46',
      '--auth-hero-soft': 'rgba(28, 47, 70, 0.08)',
      '--auth-shadow': '0 28px 60px rgba(28, 58, 92, 0.18)',
      '--auth-card-radius': '28px',
      '--auth-button-radius': '999px',
      '--auth-display-font': '"Source Han Serif SC", "Songti SC", serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'playful',
    name: '样例 B / 青年潮流',
    tag: '更活、更年轻',
    summary: '奶油橙、珊瑚红、明亮留白，视觉更有冲击，适合偏年轻的电商气质。',
    heroEyebrow: 'DROP / NEW MEMBER ACCESS',
    heroTitle: '让登录页像一张正在发售的限定活动海报。',
    heroSubtitle:
      '标题更大，背景更有层次，信息区用贴纸化卡片做节奏，整体更有年轻用户会停留的页面气质。',
    heroNote: '适合想让首页入口更有传播感和新鲜感的场景。',
    formBadge: 'Bright Drop',
    loginTitle: '回来继续逛新品',
    loginDescription: '保留活力感，但表单依旧够稳，不会影响使用。',
    registerTitle: '加入 YU-Mall 新玩家计划',
    registerDescription: '注册页更像活动报名页，第一眼就知道重点在哪。',
    metrics: [
      { label: '视觉张力', value: 'High' },
      { label: '年轻感', value: '18-28' },
      { label: '传播性', value: '更强' }
    ],
    highlights: [
      { title: '撞色结构', description: '不是单纯一层背景渐变，而是海报式分层背景。' },
      { title: '大标题先行', description: '视觉先抓人，再让用户自然进入表单动作。' },
      { title: '轻插画感', description: '不用外部素材，也能通过几何元素做出潮流感。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(140deg, #fff8ef 0%, #ffd7bf 45%, #ff9b73 100%)',
      '--auth-surface': 'rgba(255, 253, 250, 0.9)',
      '--auth-border': 'rgba(233, 91, 46, 0.18)',
      '--auth-text': '#2f1a14',
      '--auth-muted': '#83594d',
      '--auth-accent': '#ef5b2e',
      '--auth-accent-soft': 'rgba(239, 91, 46, 0.12)',
      '--auth-hero': '#3e241c',
      '--auth-hero-soft': 'rgba(62, 36, 28, 0.09)',
      '--auth-shadow': '0 28px 54px rgba(196, 85, 38, 0.2)',
      '--auth-card-radius': '32px',
      '--auth-button-radius': '20px',
      '--auth-display-font': '"YouYuan", "Microsoft YaHei", sans-serif',
      '--auth-body-font': '"Trebuchet MS", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'minimal',
    name: '样例 C / 极简高级',
    tag: '留白与秩序',
    summary: '黑白灰加一处品牌色，最克制、最清爽，适合强调专业和秩序感。',
    heroEyebrow: 'YU-MALL / AUTH SYSTEM',
    heroTitle: '去掉噪音，让登录本身看起来更高级。',
    heroSubtitle:
      '用版式、比例和留白做高级感，不靠重色块和强装饰，适合偏理性、偏专业的产品印象。',
    heroNote: '适合想要“高级但不花哨”的主视觉方向。',
    formBadge: 'Minimal Form',
    loginTitle: '登录你的商城账号',
    loginDescription: '视觉极简，但按钮、输入态和信息层级更精确。',
    registerTitle: '创建一个全新的账号',
    registerDescription: '注册流程更安静，字段布局更适合长期维护。',
    metrics: [
      { label: '噪音控制', value: 'Low' },
      { label: '专业感', value: 'High' },
      { label: '维护成本', value: '更低' }
    ],
    highlights: [
      { title: '版式说话', description: '通过留白、分割线和比例关系拉开高级感。' },
      { title: '品牌色点到为止', description: '只在按钮与关键标签使用强调色，不会花。' },
      { title: '适配轻松', description: '后续衍生找回密码、短信登录也更好接。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #f7f5f1 0%, #ece8df 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.9)',
      '--auth-border': 'rgba(31, 31, 31, 0.1)',
      '--auth-text': '#1f1f1f',
      '--auth-muted': '#686868',
      '--auth-accent': '#145c4a',
      '--auth-accent-soft': 'rgba(20, 92, 74, 0.1)',
      '--auth-hero': '#141414',
      '--auth-hero-soft': 'rgba(20, 20, 20, 0.06)',
      '--auth-shadow': '0 26px 52px rgba(37, 37, 37, 0.12)',
      '--auth-card-radius': '24px',
      '--auth-button-radius': '14px',
      '--auth-display-font': '"Bahnschrift", "PingFang SC", sans-serif',
      '--auth-body-font': '"Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'mallGlass',
    name: '贴合 1 / 蓝青玻璃商城',
    tag: '贴合现有导航与搜索',
    summary: '直接沿用当前用户端的蓝青渐变 Logo、玻璃导航、圆角搜索框语言，落地阻力最小。',
    heroEyebrow: 'YU-MALL / GLASS COMMERCE',
    heroTitle: '把你现有首页的玻璃导航和蓝青品牌感，收进认证页。',
    heroSubtitle:
      '主视觉不再另起炉灶，而是把导航栏的蓝青渐变、透明磨砂和圆角搜索感继续往下延伸，和当前用户端更连贯。',
    heroNote: '适合你现在这套站点，改完不会像突然换了一家产品。',
    formBadge: 'Glass Mall',
    loginTitle: '登录后继续逛你收藏的好物',
    loginDescription: '搜索、购物车、用户中心的蓝青语言统一延续到认证入口。',
    registerTitle: '用同一套商城语言完成注册',
    registerDescription: '注册页也保持玻璃感和轻盈蓝色，不和现有首页打架。',
    metrics: [
      { label: '现有一致性', value: 'High' },
      { label: '改造成本', value: '低' },
      { label: '上线风险', value: '小' }
    ],
    highlights: [
      { title: '沿用蓝青品牌色', description: '直接贴合现有 logo 渐变和搜索按钮颜色。' },
      { title: '玻璃导航延续', description: '把首页磨砂顶部条的感觉自然延伸到登录页。' },
      { title: '表单更像站内模块', description: '不是独立模板页，更像用户端的一部分。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #edf7ff 0%, #d9f1f4 42%, #a8d7ec 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.76)',
      '--auth-border': 'rgba(54, 177, 214, 0.18)',
      '--auth-text': '#163349',
      '--auth-muted': '#55758a',
      '--auth-accent': '#36a9d6',
      '--auth-accent-soft': 'rgba(54, 169, 214, 0.12)',
      '--auth-hero': '#12324a',
      '--auth-hero-soft': 'rgba(18, 50, 74, 0.08)',
      '--auth-shadow': '0 28px 58px rgba(50, 122, 167, 0.18)',
      '--auth-card-radius': '30px',
      '--auth-button-radius': '999px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'promoDeck',
    name: '贴合 2 / 首页会场促销',
    tag: '贴合首页活动卡片',
    summary: '把首页的会场卡片、价格标签、促销氛围带进登录注册页，更像“活动入口”。',
    heroEyebrow: 'YU-MALL / PROMO ENTRY',
    heroTitle: '让认证页直接接上首页“限时特惠”和活动会场的气氛。',
    heroSubtitle:
      '延续你首页 Banner、促销卡片、价格标签和分类活动入口的视觉节奏，让登录页也像一次正在进行的活动会场。',
    heroNote: '适合你想让入口更像电商活动页，而不是纯工具页。',
    formBadge: 'Promo Deck',
    loginTitle: '登录后继续抢限时特惠',
    loginDescription: '视觉更像首页促销卡片，购买冲动更强，电商感更直接。',
    registerTitle: '注册后立刻进入活动会场',
    registerDescription: '适合强调新用户福利、限时券和活动参与感。',
    metrics: [
      { label: '电商氛围', value: 'Strong' },
      { label: '活动感', value: 'High' },
      { label: '连贯程度', value: '高' }
    ],
    highlights: [
      { title: '承接首页卡片', description: '视觉上更接近现有 hero 和右侧促销卡片。' },
      { title: '价格感更强', description: '色彩与按钮更像商品卡和活动标签。' },
      { title: '更适合拉新', description: '注册页更像新人会场，而不是普通表单。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #fff6ef 0%, #ffe4d4 44%, #ffc3b1 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.84)',
      '--auth-border': 'rgba(248, 111, 73, 0.18)',
      '--auth-text': '#351d17',
      '--auth-muted': '#885e55',
      '--auth-accent': '#f56c6c',
      '--auth-accent-soft': 'rgba(245, 108, 108, 0.12)',
      '--auth-hero': '#41231b',
      '--auth-hero-soft': 'rgba(65, 35, 27, 0.08)',
      '--auth-shadow': '0 28px 56px rgba(209, 101, 74, 0.2)',
      '--auth-card-radius': '28px',
      '--auth-button-radius': '18px',
      '--auth-display-font': '"Microsoft YaHei", "PingFang SC", sans-serif',
      '--auth-body-font': '"Microsoft YaHei", "PingFang SC", sans-serif'
    }
  },
  {
    id: 'trustMart',
    name: '市场 1 / 高信任商城',
    tag: '市场常见高信任结构',
    summary: '强调“正品、快、省、服务”的电商入口，属于最稳的老牌平台型登录结构。',
    heroEyebrow: 'TRUST / FAST / VALUE',
    heroTitle: '先讲清平台信任，再让用户登录。',
    heroSubtitle:
      '很多成熟电商入口都不是先炫技，而是先把“平台可靠、配送快、价格稳、售后明确”传达清楚，再收口到登录动作。',
    heroNote: '适合希望入口更像成熟大平台，而不是品牌海报的方向。',
    formBadge: 'Trusted Mall',
    loginTitle: '登录查看订单、优惠券和购物车',
    loginDescription: '表单稳定、结构清晰，更像成熟电商长期使用的入口。',
    registerTitle: '注册后即刻享受平台权益',
    registerDescription: '更强调平台承诺和新用户权益，不靠花哨造型。',
    metrics: [
      { label: '品类齐全', value: '多' },
      { label: '履约效率', value: '快' },
      { label: '平台保障', value: '稳' }
    ],
    highlights: [
      { title: '信任优先', description: '先给用户平台确定感，再推进登录动作。' },
      { title: '信息密度合理', description: '不会太空，也不会像营销页一样过载。' },
      { title: '适合长期运营', description: '对新老用户都稳定，不容易审美疲劳。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #fff5f4 0%, #ffe1dd 45%, #f8b6a8 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.9)',
      '--auth-border': 'rgba(226, 70, 47, 0.16)',
      '--auth-text': '#2d1715',
      '--auth-muted': '#7e5651',
      '--auth-accent': '#d93c2f',
      '--auth-accent-soft': 'rgba(217, 60, 47, 0.11)',
      '--auth-hero': '#351a17',
      '--auth-hero-soft': 'rgba(53, 26, 23, 0.08)',
      '--auth-shadow': '0 28px 52px rgba(164, 63, 44, 0.18)',
      '--auth-card-radius': '24px',
      '--auth-button-radius': '12px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'fastPass',
    name: '市场 2 / 低摩擦快捷',
    tag: '市场常见低摩擦结构',
    summary: '强调一键回访、少打字、快速进入订单和结算，适合转化优先的认证入口。',
    heroEyebrow: 'FAST ACCESS / RETURN FLOW',
    heroTitle: '把登录做成“回到购物流程”的捷径，而不是额外步骤。',
    heroSubtitle:
      '当前成熟电商越来越强调少阻力登录，核心不是装饰，而是让用户尽快回到购物车、订单、结算和复购动作。',
    heroNote: '适合以后扩展短信验证码、社交登录和免密登录的方向。',
    formBadge: 'Fast Pass',
    loginTitle: '快速登录，继续你的购物流程',
    loginDescription: '更适合把登录看成流程中转站，而不是品牌展示页。',
    registerTitle: '更轻、更快地完成账号创建',
    registerDescription: '注册区更像一步完成的入口，适合以后继续减字段。',
    metrics: [
      { label: '回流速度', value: 'Fast' },
      { label: '输入阻力', value: 'Low' },
      { label: '扩展空间', value: '高' }
    ],
    highlights: [
      { title: '流程优先', description: '先帮助用户回到购物流程，再谈视觉表达。' },
      { title: '适合拓展免密', description: '后续接验证码、社交登录都比较顺。' },
      { title: '结算导向明显', description: '适合购物车和支付前的登录场景。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #f2f8f5 0%, #dff0e6 45%, #b8dbc5 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.9)',
      '--auth-border': 'rgba(49, 129, 89, 0.14)',
      '--auth-text': '#173125',
      '--auth-muted': '#577060',
      '--auth-accent': '#2c8c5c',
      '--auth-accent-soft': 'rgba(44, 140, 92, 0.1)',
      '--auth-hero': '#1f3c2d',
      '--auth-hero-soft': 'rgba(31, 60, 45, 0.07)',
      '--auth-shadow': '0 26px 52px rgba(59, 110, 80, 0.16)',
      '--auth-card-radius': '22px',
      '--auth-button-radius': '12px',
      '--auth-display-font': '"Segoe UI", "PingFang SC", sans-serif',
      '--auth-body-font': '"Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'memberPerks',
    name: '市场 3 / 会员权益型',
    tag: '市场常见会员拉新结构',
    summary: '把注册入口做成会员欢迎页，突出新人券、积分和权益，对注册转化更友好。',
    heroEyebrow: 'WELCOME / MEMBER BENEFITS',
    heroTitle: '不只让用户注册，而是让用户一眼看到“注册能得到什么”。',
    heroSubtitle:
      '很多消费品牌的注册入口会优先展示新人权益、会员积分、专属折扣和复购利益，重点是把注册理由讲清楚。',
    heroNote: '适合你后续想把新用户券、成长值、会员等级一起接进来。',
    formBadge: 'Member Perks',
    loginTitle: '登录领取你的专属权益',
    loginDescription: '登录页也可承接积分、优惠券、订单和会员权益的入口感。',
    registerTitle: '注册即解锁新人券与会员礼遇',
    registerDescription: '最适合强调拉新、促活和注册转化的视觉方向。',
    metrics: [
      { label: '新人券', value: '¥88' },
      { label: '会员积分', value: '3x' },
      { label: '复购动力', value: 'Strong' }
    ],
    highlights: [
      { title: '权益先展示', description: '先给利益点，再让用户填写注册信息。' },
      { title: '更适合拉新', description: '和纯登录工具页相比，更能支撑注册转化。' },
      { title: '适合做会员体系', description: '后续接等级、积分、优惠券都很自然。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #fff7f5 0%, #fde4de 42%, #f4c6bb 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.86)',
      '--auth-border': 'rgba(189, 98, 82, 0.16)',
      '--auth-text': '#351f1d',
      '--auth-muted': '#7f625e',
      '--auth-accent': '#c66b5f',
      '--auth-accent-soft': 'rgba(198, 107, 95, 0.12)',
      '--auth-hero': '#432523',
      '--auth-hero-soft': 'rgba(67, 37, 35, 0.08)',
      '--auth-shadow': '0 28px 56px rgba(141, 83, 74, 0.17)',
      '--auth-card-radius': '26px',
      '--auth-button-radius': '999px',
      '--auth-display-font': '"Georgia", "PingFang SC", serif',
      '--auth-body-font': '"Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'promoTrustBlend',
    name: '融合 / 会场信任正式版',
    tag: '贴合 2 + 市场 1',
    summary: '保留首页会场促销氛围，同时加入成熟平台的高信任表达，更接近可直接落地的正式版。',
    heroEyebrow: 'YU-MALL / PROMO TRUST ENTRY',
    heroTitle: '既像正在进行的商城会场，也像一个让人放心登录的大平台入口。',
    heroSubtitle:
      '左侧延续你首页活动会场、价格标签和促销气氛，右侧不再走纯营销页，而是加入平台保障、订单权益和服务承诺，兼顾电商氛围与信任感。',
    heroNote: '这套就是把“贴合 2”和“市场 1”往正式版收口后的方向。',
    formBadge: 'Promo Trust',
    loginTitle: '登录继续逛会场，也继续你的订单与权益',
    loginDescription: '既有活动感，也有平台感，适合作为正式登录页长期使用。',
    registerTitle: '注册后立即进入会场，并解锁平台新人权益',
    registerDescription: '适合正式注册页，既能拉新，又不会显得像纯活动页。',
    metrics: [
      { label: '活动氛围', value: 'Strong' },
      { label: '平台信任', value: 'High' },
      { label: '正式程度', value: 'Ready' }
    ],
    highlights: [
      { title: '会场感保留', description: '继续承接首页 Banner、价格标签和促销入口的氛围。' },
      { title: '信任信息补强', description: '加入平台保障、订单权益和服务承诺的表达逻辑。' },
      { title: '更像正式成品', description: '不再偏活动页或模板页，而是更接近最终落地版本。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #fff4ef 0%, #ffe0d2 38%, #f3c9b8 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.88)',
      '--auth-border': 'rgba(221, 82, 56, 0.16)',
      '--auth-text': '#321917',
      '--auth-muted': '#7e5c55',
      '--auth-accent': '#e2583e',
      '--auth-accent-soft': 'rgba(226, 88, 62, 0.12)',
      '--auth-hero': '#3d1f1a',
      '--auth-hero-soft': 'rgba(61, 31, 26, 0.08)',
      '--auth-shadow': '0 28px 56px rgba(168, 79, 57, 0.18)',
      '--auth-card-radius': '26px',
      '--auth-button-radius': '16px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'promoClean',
    name: '精简版 / 简洁促销',
    tag: '保留促销感，但先把认证做干净',
    summary: '暖橙活动氛围保留，但左侧只留标题、说明和 3 条权益，不再堆活动卡片。',
    heroEyebrow: 'YU-MALL / CLEAN PROMO',
    heroTitle: '把活动感收住，让登录和注册先把主流程讲清楚。',
    heroSubtitle:
      '继续沿用首页暖橙色和电商氛围，但信息层级压缩到可快速扫读的程度，避免像活动落地页一样过满。',
    heroNote: '适合正式上线，既不空，也不会再显得杂乱。',
    formBadge: 'Clean Promo',
    loginTitle: '登录后继续查看活动与订单',
    loginDescription: '表单优先，权益表达退到辅助层，不再和主按钮抢注意力。',
    registerTitle: '注册后领取新人权益并继续逛商城',
    registerDescription: '保留一点拉新氛围，但注册动作本身更直接、更清爽。',
    bullets: ['同步购物车与订单', '新人券和活动价继续保留', '手机端和桌面端都更干净'],
    metrics: [],
    highlights: [],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #fff8f3 0%, #fee9de 45%, #ffd0bc 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.9)',
      '--auth-border': 'rgba(240, 121, 86, 0.14)',
      '--auth-text': '#2f1c17',
      '--auth-muted': '#7d6058',
      '--auth-accent': '#ef734e',
      '--auth-accent-soft': 'rgba(239, 115, 78, 0.1)',
      '--auth-hero': '#39211b',
      '--auth-hero-soft': 'rgba(57, 33, 27, 0.06)',
      '--auth-shadow': '0 24px 48px rgba(186, 100, 71, 0.16)',
      '--auth-card-radius': '24px',
      '--auth-button-radius': '14px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'pureMinimal',
    name: '极简版 / 纯净认证',
    tag: '接近纯工具型登录页',
    summary: '去掉促销表达，只保留品牌识别、留白和表单层级，主操作最集中。',
    heroEyebrow: 'YU-MALL / PURE AUTH',
    heroTitle: '登录和注册只做一件事：快速完成认证。',
    heroSubtitle:
      '不再强调活动、权益和会场氛围，只用留白、比例和轻微品牌色把界面收成一个稳定入口。',
    heroNote: '最稳，最安静，也最适合长期维护。',
    formBadge: 'Pure Minimal',
    loginTitle: '登录你的商城账号',
    loginDescription: '信息只留下必要内容，按钮、输入框和跳转关系清晰到位。',
    registerTitle: '创建一个新的商城账号',
    registerDescription: '注册流程不再承担营销表达，重点回到建号本身。',
    bullets: ['标题更短', '说明更少', '表单权重最高'],
    metrics: [],
    highlights: [],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #f7f7f5 0%, #efeee9 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.94)',
      '--auth-border': 'rgba(24, 31, 38, 0.08)',
      '--auth-text': '#181f26',
      '--auth-muted': '#68707a',
      '--auth-accent': '#1f5b92',
      '--auth-accent-soft': 'rgba(31, 91, 146, 0.08)',
      '--auth-hero': '#161b22',
      '--auth-hero-soft': 'rgba(22, 27, 34, 0.04)',
      '--auth-shadow': '0 20px 40px rgba(18, 24, 31, 0.08)',
      '--auth-card-radius': '18px',
      '--auth-button-radius': '10px',
      '--auth-display-font': '"Bahnschrift", "PingFang SC", "Microsoft YaHei", sans-serif',
      '--auth-body-font': '"Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'homeShowcase',
    name: '新样例 1 / 首页会场双栏',
    tag: '最贴合当前首页 Banner 与促销卡',
    summary: '直接承接首页蓝色玻璃感、促销卡片和大 Banner，左侧像会场入口，右侧是干净表单。',
    heroEyebrow: 'YU-MALL / HOME SHOWCASE',
    heroTitle: '把当前首页的会场氛围，直接延伸成全屏双栏登录页。',
    heroSubtitle:
      '左侧像首页会场主视觉，延续玻璃面板、促销标签和蓝色空气感；右侧表单保持简洁，重点放在登录与注册动作本身。',
    heroNote: '这一套最接近你现有用户端首页，不会让登录页看起来像另一个产品。',
    formBadge: 'Home Showcase',
    loginTitle: '登录后继续逛会场与活动专区',
    loginDescription: '保留首页主视觉氛围，但登录入口本身更清楚，适合正式落地。',
    registerTitle: '注册后立刻领取新人权益并继续逛首页',
    registerDescription: '注册框同步增加昵称字段，整体仍然保持首页系的商城气质。',
    bullets: ['全屏双栏结构', '延续首页玻璃感与促销标签', '注册框包含昵称字段'],
    metrics: [
      { label: '首页会场感', value: 'High' },
      { label: '品牌一致性', value: 'Strong' },
      { label: '转化入口', value: 'Clear' }
    ],
    highlights: [
      { title: 'Banner 延续', description: '左侧更像首页会场而不是孤立认证页。' },
      { title: '促销节奏保留', description: '保留一点商城活动感，但不再堆满信息。' },
      { title: '正式页可直接落地', description: '改成真正的 /login 和 /register 风险最低。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #ecf6ff 0%, #dfeefe 45%, #c9dfff 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.84)',
      '--auth-border': 'rgba(64, 158, 255, 0.16)',
      '--auth-text': '#18344f',
      '--auth-muted': '#5f7890',
      '--auth-accent': '#409eff',
      '--auth-accent-soft': 'rgba(64, 158, 255, 0.12)',
      '--auth-hero': '#17324b',
      '--auth-hero-soft': 'rgba(23, 50, 75, 0.08)',
      '--auth-shadow': '0 28px 58px rgba(53, 111, 178, 0.18)',
      '--auth-card-radius': '28px',
      '--auth-button-radius': '16px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'categoryGlass',
    name: '新样例 2 / 分类玻璃橱窗',
    tag: '最贴合当前分类侧栏与玻璃面板',
    summary: '把首页分类侧栏、玻璃卡片和清爽蓝青色延伸到登录/注册，整体更轻、更像站内模块。',
    heroEyebrow: 'YU-MALL / CATEGORY GLASS',
    heroTitle: '让认证页像商城首页的玻璃橱窗，而不是独立模板页。',
    heroSubtitle:
      '左侧更偏分类导航与商城入口感，颜色更轻、更透明，适合想保留当前站内界面语言，又不想太像促销落地页的方向。',
    heroNote: '这一套比会场版更轻，更适合长期在线上跑，不容易显得太营销。',
    formBadge: 'Category Glass',
    loginTitle: '登录后继续浏览分类、收藏和购物车',
    loginDescription: '整体像站内内容模块，和现有首页、分类、商品列表更统一。',
    registerTitle: '注册你的商城账号并完善基础信息',
    registerDescription: '注册框增加昵称字段，整体维持轻玻璃、清爽蓝青的电商感。',
    bullets: ['更轻的玻璃风格', '更贴近首页分类与筛选条', '更适合长期维护'],
    metrics: [
      { label: '轻盈程度', value: 'Light' },
      { label: '首页贴合', value: 'High' },
      { label: '审美疲劳', value: 'Low' }
    ],
    highlights: [
      { title: '分类语言延续', description: '更像首页左侧分类区与筛选条的延伸。' },
      { title: '表单存在感刚好', description: '不会过度营销，也不会太工具化。' },
      { title: '移动端更稳', description: '收缩到单栏后依然比较自然。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #eff9fb 0%, #dff1f4 42%, #c7e4ec 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.78)',
      '--auth-border': 'rgba(54, 169, 214, 0.16)',
      '--auth-text': '#143348',
      '--auth-muted': '#5d788a',
      '--auth-accent': '#36a9d6',
      '--auth-accent-soft': 'rgba(54, 169, 214, 0.11)',
      '--auth-hero': '#12324a',
      '--auth-hero-soft': 'rgba(18, 50, 74, 0.08)',
      '--auth-shadow': '0 28px 54px rgba(63, 132, 163, 0.17)',
      '--auth-card-radius': '30px',
      '--auth-button-radius': '999px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  },
  {
    id: 'cartReturn',
    name: '新样例 3 / 购物车回流入口',
    tag: '最偏下单回流与高信任入口',
    summary: '更强调继续结算、继续购物车、继续订单的回流逻辑，适合登录是流程节点的商城。',
    heroEyebrow: 'YU-MALL / RETURN FLOW',
    heroTitle: '把登录做成回到购物车、订单和结算的中转入口。',
    heroSubtitle:
      '如果你的用户更多是在加购、结算、查订单时被要求登录，这套会更合理。左侧不做夸张活动，而是强调平台信任、下单流和账户回流。',
    heroNote: '这一套更像正式商业化认证页，适合把登录页当成流程的一部分。',
    formBadge: 'Return Flow',
    loginTitle: '登录后继续你的购物车与订单流程',
    loginDescription: '重点突出账户回流、下单延续和平台信任，更适合正式登录入口。',
    registerTitle: '注册账号后继续你的购物流程',
    registerDescription: '注册框仍然包含昵称字段，但整体更偏成熟平台，而不是活动页。',
    bullets: ['更强调购物车与订单回流', '更稳的高信任平台感', '适合正式登录入口'],
    metrics: [
      { label: '回流效率', value: 'Fast' },
      { label: '平台信任', value: 'High' },
      { label: '流程一致性', value: 'Strong' }
    ],
    highlights: [
      { title: '更像正式入口', description: '不是活动页，而是订单流程上的认证页。' },
      { title: '适合购物车拦截', description: '和当前电商购买链路更匹配。' },
      { title: '注册也不突兀', description: '注册页同步同一套成熟风格。' }
    ],
    variables: {
      '--auth-bg': 'linear-gradient(135deg, #f5f8fc 0%, #e3edf8 45%, #d4e0f1 100%)',
      '--auth-surface': 'rgba(255, 255, 255, 0.88)',
      '--auth-border': 'rgba(70, 106, 148, 0.14)',
      '--auth-text': '#1b2f44',
      '--auth-muted': '#627385',
      '--auth-accent': '#4c79a6',
      '--auth-accent-soft': 'rgba(76, 121, 166, 0.1)',
      '--auth-hero': '#1c3148',
      '--auth-hero-soft': 'rgba(28, 49, 72, 0.08)',
      '--auth-shadow': '0 28px 54px rgba(67, 91, 124, 0.16)',
      '--auth-card-radius': '24px',
      '--auth-button-radius': '12px',
      '--auth-display-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif',
      '--auth-body-font': '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    }
  }
]

export const defaultAuthPreviewThemeId: AuthPreviewThemeId = 'luxe'
