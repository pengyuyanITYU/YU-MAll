export type AuthPromoMode = 'login' | 'register'

interface AuthPromoStat {
  value: string
  label: string
}

interface AuthPromoCard {
  label: string
  title: string
  description: string
}

export interface AuthPromoContent {
  eyebrow: string
  title: string
  subtitle: string
  note: string
  panelBadge: string
  chips: string[]
  offerLabel: string
  offerValue: string
  offerNote: string
  stats: AuthPromoStat[]
  cards: AuthPromoCard[]
}

export const authPromoContent: Record<AuthPromoMode, AuthPromoContent> = {
  login: {
    eyebrow: 'YU-MALL / PROMO ENTRY',
    title: '限时会场、加购好物、爆款直降，登录后继续抢。',
    subtitle:
      '把首页活动会场的热度接到登录页，用户不会觉得自己跳出了商城场景，登录动作自然成为促销链路的一部分。',
    note: '左侧负责营造正在进行中的电商活动氛围，右侧只保留最高频、最直接的认证动作。',
    panelBadge: '限时会场入口',
    chips: ['今日爆款', '新人券包', '满99包邮'],
    offerLabel: '会场低至',
    offerValue: '5 折起',
    offerNote: '距本场结束 02 : 18 : 46',
    stats: [
      { value: '300+', label: '热卖单品' },
      { value: '¥188', label: '新人券包' },
      { value: '24h', label: '返场专区' }
    ],
    cards: [
      {
        label: '加购回流',
        title: '登录后同步购物车',
        description: '继续支付未完成订单，不丢刚刚挑中的商品。'
      },
      {
        label: '活动提醒',
        title: '会场价实时可见',
        description: '热门类目、新人专区和包邮场次统一回流。'
      },
      {
        label: '权益到账',
        title: '优惠券自动入账',
        description: '登录后即可领取活动券、首单礼和会员价。'
      }
    ]
  },
  register: {
    eyebrow: 'YU-MALL / NEW MEMBER',
    title: '新用户先领券，再去逛会场，注册入口也要带着促销感。',
    subtitle:
      '注册页沿用首页活动节奏，但把流程压到最短，先完成建号，再进入会场抢新人礼和爆款专区。',
    note: '界面只保留必要输入项，昵称和头像由前端补默认值，用户后续在个人中心再完善资料。',
    panelBadge: '新人专享入口',
    chips: ['首单立减', '爆款专区', '7天价保'],
    offerLabel: '新人礼',
    offerValue: '¥188',
    offerNote: '注册成功自动到账',
    stats: [
      { value: '3 步', label: '完成建号' },
      { value: '¥188', label: '券包到账' },
      { value: '首单', label: '包邮福利' }
    ],
    cards: [
      {
        label: '建号更轻',
        title: '表单只保留必填动作',
        description: '账号、手机号、密码一次填完，进入会场不被额外步骤拖慢。'
      },
      {
        label: '福利直达',
        title: '注册成功自动带上新人权益',
        description: '新人礼、会场券和首单活动直接进入你的账号。'
      },
      {
        label: '资料后补',
        title: '头像昵称后续再完善',
        description: '先让用户完成进场动作，个人资料放到用户中心慢慢补齐。'
      }
    ]
  }
}
