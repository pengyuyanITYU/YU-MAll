<template>
  <div class="user-center-page">
    <div class="container">
      <el-row :gutter="24" class="main-row">
        
        <!-- 左侧侧边栏 -->
        <el-col :xs="24" :sm="8" :md="6" :lg="6">
          <div class="profile-sidebar">
            <div class="profile-header">
              <div class="avatar-box">
                <el-avatar :size="80" :src="user.avatar" class="user-avatar" />
                <img src="https://cdn-icons-png.flaticon.com/512/6941/6941697.png" class="vip-badge" alt="vip"/>
              </div>
              <h2 class="username">{{ user.nickname }}</h2>
              <div class="user-id">ID: {{ user.id }}</div>
              
              <div class="vip-progress-box">
                <div class="level-row">
                  <span class="curr-lv">Lv.{{ user.level }}</span>
                  <span class="next-lv">Lv.{{ user.level + 1 }}</span>
                </div>
                <el-progress 
                  :percentage="user.expPercentage" 
                  :show-text="false" 
                  color="#e6a23c" 
                  :stroke-width="6"
                />
                <div class="exp-text">再积累 {{ user.nextLevelExp - user.exp }} 成长值升级</div>
              </div>
            </div>

            <div class="menu-list">
              <el-menu
                :default-active="currentTab"
                class="user-nav-menu"
                @select="handleNavSelect"
                mode="vertical" 
                background-color="transparent"
                text-color="#606266"
                active-text-color="#409eff"
                unique-opened
              >
                <el-menu-item index="dashboard">
                  <el-icon class="menu-icon"><User /></el-icon>
                  <span>账户概览</span>
                </el-menu-item>
                
                <!-- ★★★ 新增：个人资料入口 ★★★ -->
                <el-menu-item index="profile">
                  <el-icon class="menu-icon"><Setting /></el-icon>
                  <span>个人资料</span>
                </el-menu-item>

                <el-menu-item index="orders">
                  <el-icon class="menu-icon"><List /></el-icon>
                  <span>我的订单</span>
                </el-menu-item>
                
                <el-menu-item index="comments">
                  <el-icon class="menu-icon"><ChatLineSquare /></el-icon>
                  <span>我的评价</span>
                </el-menu-item>

                <el-menu-item index="cart">
                  <el-icon class="menu-icon"><ShoppingCart /></el-icon>
                  <span>我的购物车</span>
                </el-menu-item>
                <el-menu-item index="favorites">
                  <el-icon class="menu-icon"><Star /></el-icon>
                  <span>我的收藏</span>
                </el-menu-item>
                <el-menu-item index="wallet">
                  <el-icon class="menu-icon"><Wallet /></el-icon>
                  <span>我的钱包</span>
                </el-menu-item>
                <el-menu-item index="address">
                  <el-icon class="menu-icon"><Location /></el-icon>
                  <span>收货地址</span>
                </el-menu-item>
              </el-menu>
            </div>
          </div>
        </el-col>

        <!-- 右侧内容区 -->
        <el-col :xs="24" :sm="16" :md="18" :lg="18">
          <div class="content-panel">
            
            <transition name="fade" mode="out-in">
              
              <!-- TAB: 账户概览 (保持不变) -->
              <div v-if="currentTab === 'dashboard'" key="dashboard" class="tab-view">
                <h3 class="panel-title">我的资产</h3>
                <el-row :gutter="20">
                  <el-col :span="12" :xs="24">
                    <div class="asset-card balance-card">
                      <div class="card-top">
                        <span class="card-label">账户余额 (元)</span>
                        <el-icon><Wallet /></el-icon>
                      </div>
                      <div class="card-num">¥ {{ formatPrice(user.balance) }}</div>
                      <div class="card-bottom">
                        <el-button size="small" round class="glass-btn">充值</el-button>
                        <el-button size="small" round class="glass-btn">提现</el-button>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="12" :xs="24">
                     <div class="stats-grid">
                       <div class="stat-box">
                         <el-icon class="stat-icon" color="#e6a23c"><Trophy /></el-icon>
                         <div class="val">{{ user.currentPoints }}</div>
                         <div class="lbl">积分</div>
                       </div>
                       <div class="stat-box">
                         <el-icon class="stat-icon" color="#f56c6c"><Ticket /></el-icon>
                         <div class="val">{{ user.coupons }}</div>
                         <div class="lbl">优惠券</div>
                       </div>
                       <div class="stat-box">
                         <el-icon class="stat-icon" color="#409eff"><StarFilled /></el-icon>
                         <div class="val">{{ user.favorites }}</div>
                         <div class="lbl">收藏夹</div>
                       </div>
                       <div class="stat-box">
                         <el-icon class="stat-icon" color="#67c23a"><Timer /></el-icon>
                         <div class="val">{{ user.footprints }}</div>
                         <div class="lbl">足迹</div>
                       </div>
                     </div>
                  </el-col>
                </el-row>

                <h3 class="panel-title" style="margin-top: 30px;">
                  最近订单
                  <el-link type="primary" :underline="false" style="font-size:13px; float:right" @click="handleNavSelect('orders')">查看全部</el-link>
                </h3>
                
                <div class="recent-orders" v-loading="loadingOrders">
                   <div v-if="recentOrders.length > 0">
                     <div v-for="order in recentOrders" :key="order.id" class="mini-order-item" @click="handleNavSelect('orders')">
                       <el-image :src="order.image" class="mini-img" fit="cover">
                          <template #error><div class="image-slot"><el-icon><Picture /></el-icon></div></template>
                       </el-image>
                       <div class="mini-info">
                         <div class="mini-title">{{ order.title }}</div>
                         <el-tag size="small" :type="order.statusType as any" effect="plain">{{ order.statusText }}</el-tag>
                       </div>
                       <div class="mini-price">¥{{ formatPrice(order.price) }}</div>
                     </div>
                   </div>
                   <el-empty v-else description="暂无最近订单" :image-size="80" />
                </div>
              </div>

              <!-- ★★★ 新增 TAB: 个人资料 ★★★ -->
              <div v-else-if="currentTab === 'profile'" key="profile" class="tab-view">
                <h3 class="panel-title">个人资料</h3>
                <div class="profile-container">
                  <el-tabs type="border-card" class="profile-tabs">
                    <!-- 子标签 1: 基本信息 -->
                    <el-tab-pane label="基本信息">
                      <el-form 
                        :model="profileForm" 
                        :rules="profileRules" 
                        ref="profileFormRef" 
                        label-width="100px"
                        class="profile-form"
                      >
                          <!-- ★★★ 修改开始：头像上传区域 ★★★ -->
    <el-form-item label="头像" prop="avatar" class="avatar-form-item">
      <div class="creative-avatar-wrapper">
        <el-upload
          class="avatar-uploader"
          action="#"
          :http-request="customUploadRequest"
          :show-file-list="false"
          :before-upload="BeforeUpload"
          accept="image/*"
        >
          <!-- 状态 A: 已上传 (有 URL 时显示) -->
          <div
            v-if="profileForm.avatar"
            class="avatar-preview-box animate__animated animate__zoomIn"
          >
            <img :src="profileForm.avatar" class="avatar-img" />
            <div class="avatar-hover-mask">
              <el-icon :size="24"><Edit /></el-icon>
              <span>更换</span>
            </div>
          </div>

          <!-- 状态 B: 上传中 (Loading) -->
          <div v-else-if="avatarLoading" class="avatar-upload-trigger">
            <div class="trigger-circle">
              <el-icon class="is-loading" :size="28"><Loading /></el-icon>
            </div>
            <div class="trigger-label">上传中...</div>
          </div>

          <!-- 状态 C: 未上传/空头像 -->
          <div v-else class="avatar-upload-trigger">
            <div class="trigger-circle">
              <el-icon :size="28" class="camera-icon"><CameraFilled /></el-icon>
            </div>
            <div class="trigger-label">设置头像</div>
          </div>
        </el-upload>
      </div>
    </el-form-item>

                        <el-form-item label="昵称" prop="nickName">
                          <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
                        </el-form-item>

                        <el-form-item label="性别" prop="gender">
                          <el-radio-group v-model="profileForm.gender">
                            <el-radio :label="1">男</el-radio>
                            <el-radio :label="0">女</el-radio>
                            <el-radio :label="2">保密</el-radio>
                          </el-radio-group>
                        </el-form-item>

                        <el-form-item label="生日" prop="birthday">
                          <el-date-picker
                            v-model="profileForm.birthday"
                            type="date"
                            placeholder="选择生日"
                            style="width: 100%"
                            format="YYYY/MM/DD"
                            value-format="YYYY/MM/DD"
                          />
                        </el-form-item>

                        <el-form-item label="手机号" prop="phone">
                          <el-input v-model="profileForm.phone"  >
                            
                          </el-input>
                        </el-form-item>

                        <el-form-item label="邮箱" prop="email">
                          <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                        </el-form-item>

                        <el-form-item>
                          <el-button type="primary" :loading="updatingProfile" @click="handleUpdateProfile">保存修改</el-button>
                          <el-button @click="resetProfileForm">重置</el-button>
                        </el-form-item>
                      </el-form>
                    </el-tab-pane>

                    <!-- 子标签 2: 修改密码 -->
                    <el-tab-pane label="安全设置">
                      <el-alert
                        title="为了保障您的账户安全，修改密码前请确认环境安全。"
                        type="info"
                        show-icon
                        :closable="false"
                        style="margin-bottom: 20px;"
                      />
                      <el-form 
                        :model="passwordForm" 
                        :rules="passwordRules" 
                        ref="passwordFormRef" 
                        label-width="100px"
                        class="password-form"
                      >
                        <!-- 隐藏域：手机号 (API需要) -->
                        <el-form-item label="手机号" >
                          <el-input v-model="passwordForm.phone" placeholder="请输入手机号" />
                        </el-form-item>

                        <el-form-item label="旧密码" prop="oldPassword">
                          <el-input 
                            v-model="passwordForm.oldPassword" 
                            type="password" 
                            show-password 
                            placeholder="请输入当前密码"
                          />
                        </el-form-item>

                        <el-form-item label="新密码" prop="newPassword">
                          <el-input 
                            v-model="passwordForm.newPassword" 
                            type="password" 
                            show-password 
                            placeholder="请输入新密码 (6-20位)"
                          />
                        </el-form-item>

                        <el-form-item label="确认密码" prop="confirmPassword">
                          <el-input 
                            v-model="passwordForm.confirmPassword" 
                            type="password" 
                            show-password 
                            placeholder="请再次输入新密码"
                          />
                        </el-form-item>

                        <el-form-item>
                          <el-button type="danger" :loading="updatingPassword" @click="handleChangePassword">确认修改密码</el-button>
                        </el-form-item>
                      </el-form>
                    </el-tab-pane>
                  </el-tabs>
                </div>
              </div>

              <!-- TAB: 我的订单 (保持不变) -->
              <div v-else-if="currentTab === 'orders'" key="orders" class="tab-view">
                <div class="section-header">
                  <h3 class="panel-title">我的订单</h3>
                  <el-input 
                    v-model="searchOrder" 
                    placeholder="搜索订单号/商品名称" 
                    style="width: 220px" 
                    :prefix-icon="Search" 
                    clearable
                  />
                </div>
                
                <el-tabs v-model="orderStatus" class="order-tabs">
                  <el-tab-pane label="全部" name="all"></el-tab-pane>
                  <el-tab-pane :label="`待付款${orderCounts.pay > 0 ? ' ('+orderCounts.pay+')' : ''}`" name="pay"></el-tab-pane>
                  <el-tab-pane :label="`待发货${orderCounts.ship > 0 ? ' ('+orderCounts.ship+')' : ''}`" name="ship"></el-tab-pane>
                  <el-tab-pane :label="`待收货${orderCounts.receive > 0 ? ' ('+orderCounts.receive+')' : ''}`" name="receive"></el-tab-pane>
                </el-tabs>
                
                <div class="order-list" v-loading="loadingOrders">
                  <div v-if="filteredOrderList.length > 0">
                    <el-card 
                      v-for="order in filteredOrderList" 
                      :key="order.id" 
                      class="order-card" 
                      shadow="hover" 
                      :body-style="{ padding: '0px' }"
                    >
                      <div class="order-header">
                        <div class="header-left">
                          <span class="order-time">{{ order.createTime }}</span>
                          <span class="order-no">订单号：{{ order.id }}</span>
                        </div>
                        <div class="header-right">
                          <el-tag :type="formatStatus(order.status).type as any" effect="dark">
                            {{ formatStatus(order.status).text }}
                          </el-tag>
                        </div>
                      </div>

                      <div class="order-body">
                        <div 
                          v-for="detail in order.details" 
                          :key="detail.id" 
                          class="order-item"
                        >
                          <el-image :src="detail.image" class="item-img" fit="cover">
                             <template #error><div class="image-slot"><el-icon><Picture /></el-icon></div></template>
                          </el-image>
                          <div class="item-info">
                            <div class="item-name">{{ detail.name }}</div>
                            <div class="item-spec" v-if="detail.spec">
                              <span v-for="(val, key) in detail.spec" :key="key" style="margin-right: 8px;">
                                {{ key }}: {{ val }}
                              </span>
                            </div>
                          </div>
                          <div class="item-price-num">
                            <div class="price">¥{{ (detail.price / 100).toFixed(2) }}</div>
                            <div class="num">x{{ detail.num }}</div>
                          </div>
                        </div>
                      </div>

                      <div class="order-footer">
                        <div class="total-section">
                          共 {{ order.details?.length || 0 }} 件商品，
                          实付款：<span class="total-price">¥{{ (order.totalFee / 100).toFixed(2) }}</span>
                        </div>
                        <div class="action-btns">
                          <el-button v-if="order.status === 1" size="small" type="primary" round plain @click="handlePay(order.id)">立即付款</el-button>
                          <el-button v-if="order.status === 3" size="small" type="success" round plain @click="handleConfirmOrder(order.id)">确认收货</el-button>
                          <el-button size="small" round @click="handleViewDetail(order.id)">查看详情</el-button>
                          <el-button 
                            v-if="[1, 4, 5].includes(order.status)" 
                            size="small" 
                            type="danger" 
                            link 
                            @click="handleDeleteOrder(order.id)"
                          >删除订单</el-button>
                        </div>
                      </div>
                    </el-card>
                  </div>
                  <el-empty v-else description="暂无符合条件的订单" />
                </div>
              </div>

              <!-- TAB: 我的评价 (保持不变) -->
              <div v-else-if="currentTab === 'comments'" key="comments" class="tab-view">
                <div class="section-header">
                  <h3 class="panel-title">我的评价</h3>
                </div>

                <div v-loading="loadingComments">
                  <div v-if="commentList.length > 0" class="comment-list">
                    <div 
                      v-for="item in commentList" 
                      :key="item.id" 
                      class="comment-card dreamy-card"
                    >
                      <div class="card-left" v-if="item.productImage">
                        <el-image 
                           :src="item.productImage" 
                           class="product-thumb" 
                           fit="cover"
                           @click="goToProductDetail(item.itemId)"
                        >
                           <template #error>
                             <div class="img-error-slot"></div>
                           </template>
                        </el-image>
                      </div>

                      <div class="card-center">
                        <div class="user-info-row">
                          <el-avatar :size="32" :src="item.userAvatar || user.avatar" class="mini-avatar"></el-avatar>
                          <div class="user-meta">
                            <span class="mini-nickname">{{ item.userNickname || user.nickname }}</span>
                            <el-rate 
                              v-model="item.rating" 
                              disabled 
                              text-color="#ff9900"
                              size="small"
                              class="mini-rate"
                            />
                          </div>
                          <span class="time">{{ item.createTime }}</span>
                        </div>

                        <div class="content-text">{{ item.content }}</div>

                        <div class="review-imgs" v-if="item.images && item.images.length">
                          <el-image 
                            v-for="(img, idx) in item.images" 
                            :key="idx"
                            :src="img"
                            :preview-src-list="item.images"
                            class="review-img-item"
                            fit="cover"
                            lazy
                          />
                        </div>

                        <div v-if="item.merchantReplyContent" class="merchant-reply-box">
                          <span class="reply-label">商家回复：</span>
                          {{ item.merchantReplyContent }}
                        </div>

                        <div class="card-footer">
                          <div class="footer-product" @click="goToProductDetail(item.itemId)">
                            <el-icon><Goods /></el-icon>
                            <span class="product-link">
                              {{ item.itemName || '查看商品详情' }}
                            </span>
                          <span class="sku-text" v-if="item.skuSpecs">
                              <el-tag 
                                class="sku-tag" 
                                type="info" 
                                effect="plain" 
                                size="small"
                              >
                                {{ formatSku(item.skuSpecs) }}
                              </el-tag>
                            </span>
                          </div>
                          
                          <div class="footer-actions">
                            <el-dropdown trigger="click" @command="handleCommand($event, item)">
                              <div class="more-btn">
                                <el-icon :size="20" color="#909399"><MoreFilled /></el-icon>
                              </div>
                              <template #dropdown>
                                <el-dropdown-menu>
                                  <el-dropdown-item command="delete" style="color: #f56c6c">
                                    <el-icon><Delete /></el-icon>删除评论
                                  </el-dropdown-item>
                                </el-dropdown-menu>
                              </template>
                            </el-dropdown>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="您还没有发表过评价" />
                </div>
              </div>

              <!-- TAB: 购物车 (保持不变) -->
              <div v-else-if="currentTab === 'cart'" key="cart" class="tab-view">
                <div class="section-header">
                  <h3 class="panel-title">我的购物车</h3>
                </div>
                
                <div v-if="cartItems.length === 0" class="empty-cart">
                  <el-empty description="购物车空空如也，快去添加商品吧" :image-size="120">
                     <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
                  </el-empty>
                </div>
                
                <div v-else class="cart-preview-container">
                   <div class="cart-list-enhanced">
                    <el-card v-for="item in cartItems" :key="item.id" class="cart-card" shadow="hover" :body-style="{ padding: '12px' }">
                      <div class="cart-item-flex">
                        <el-image :src="item.image" class="cart-thumb" fit="cover">
                           <template #error><div class="image-slot"><el-icon><Picture /></el-icon></div></template>
                        </el-image>
                        
                        <div class="cart-details">
                          <div class="cart-title">{{ item.name }}</div>
                          <div class="cart-specs" v-if="item.specs">
                             <el-tag size="small" type="info" effect="plain">默认规格</el-tag>
                          </div>
                        </div>
                        
                        <div class="cart-price-col">
                          <div class="price">¥{{ formatPrice(item.price) }}</div>
                          <div class="qty">x {{ item.quantity }}</div>
                        </div>
                      </div>
                    </el-card>
                   </div>
                   
                   <div class="cart-action-bar">
                      <div class="total-info">
                        共 <span>{{ cartItems.length }}</span> 件商品
                      </div>
                      <el-button type="primary" color="#409eff" round size="large" @click="router.push('/cart')">
                        去购物车结算 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                      </el-button>
                   </div>
                </div>
              </div>
              
              <!-- TAB: 收藏 (保持不变) -->
              <div v-else-if="currentTab === 'favorites'" key="favorites" class="tab-view">
                <div class="section-header">
                  <h3 class="panel-title">我的收藏</h3>
                </div>
                
                <div v-loading="loadingFavorites">
                  <div v-if="favoriteList.length > 0">
                    <el-row :gutter="16">
                      <el-col 
                        v-for="item in favoriteList" 
                        :key="item.id" 
                        :xs="12" :sm="8" :md="8" :lg="6"
                      >
                        <el-card class="fav-card" shadow="hover" :body-style="{ padding: '0px' }">
                          <div class="fav-img-box" @click="goToProductDetail(item.itemId)">
                            <el-image :src="item.image" fit="cover" class="fav-img" lazy>
                              <template #error><div class="image-slot"><el-icon><Picture /></el-icon></div></template>
                            </el-image>
                            <div class="fav-overlay">
                              <el-button type="primary" size="small" round @click.stop="goToProductDetail(item.itemId)">查看</el-button>
                              <el-button type="danger" size="small" round @click.stop="handleDeleteFavorite(item.itemId)">删除</el-button>
                            </div>
                          </div>
                          <div class="fav-info">
                            <div class="fav-title" :title="item.name || item.title">{{ item.name }}</div>
                             <div class="fav-tags">
                              <span v-if="item.price < 9900" class="tag-item blue">包邮</span>
                              <span v-if="item.price > 20000" class="tag-item red">满减券</span>
                              <span v-if="!item.freeShipping && !item.coupon" class="tag-item gray">热销好物</span> 
                            </div>
                            <div class="fav-price">¥{{ formatPrice(item.price) }}</div>
                            <div class="fav-time" v-if="item.createTime">收藏于 {{ item.createTime.split(' ')[0] }}</div>
                          </div>
                        </el-card>
                      </el-col>
                    </el-row>
                  </div>
                  <el-empty v-else description="暂无收藏商品，快去添加吧" :image-size="120">
                     <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
                  </el-empty>
                </div>
              </div>

              <!-- TAB: 钱包 (保持不变) -->
              <div v-else-if="currentTab === 'wallet'" key="wallet" class="tab-view">
                <div class="section-header"><h3 class="panel-title">我的钱包</h3></div>
                <div class="asset-card balance-card" style="margin-bottom: 30px;">
                  <div class="card-top">
                    <span class="card-label">账户余额 (元)</span>
                    <el-icon><Wallet /></el-icon>
                  </div>
                  <div class="card-num">¥ {{ formatPrice(user.balance) }}</div>
                  <div class="card-bottom">
                    <el-button size="small" round class="glass-btn">充值</el-button>
                    <el-button size="small" round class="glass-btn">提现</el-button>
                  </div>
                </div>
                <div class="transaction-section">
                  <h4 class="section-title">交易记录</h4>
                  <div class="transaction-list">
                    <div v-for="transaction in filteredTransactions" :key="transaction.id" class="transaction-item">
                      <div class="transaction-info">
                        <div class="transaction-title">{{ transaction.title }}</div>
                        <div class="transaction-time">{{ transaction.time }}</div>
                      </div>
                      <div class="transaction-amount" :class="transaction.type">
                        {{ transaction.amount > 0 ? '+' : '' }}{{ transaction.amount.toFixed(2) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- TAB: 地址管理 (保持不变) -->
              <div v-else-if="currentTab === 'address'" key="address" class="tab-view">
                <div class="section-header">
                  <h3 class="panel-title">收货地址</h3>
                  <el-button type="primary" icon="Plus" round @click="openAddAddressDialog">新增地址</el-button>
                </div>
                
                <el-row :gutter="20" v-loading="loadingAddress">
                  <el-col v-for="addr in addresses" :key="addr.id" :xs="24" :sm="12" :md="12" :lg="12">
                    <div class="address-card" :class="{ default: addr.isDefault }">
                      <div class="addr-header">
                        <span class="name">{{ addr.contact || addr.name }}</span> 
                        <span class="phone">{{ addr.mobile || addr.phone }}</span>
                        <el-tag v-if="addr.notes || addr.tag" size="small" effect="plain" type="info">{{ addr.notes || addr.tag }}</el-tag>
                        <el-tag v-if="addr.isDefault" size="small" type="danger" effect="dark" class="default-tag">默认</el-tag>
                      </div>
                      <div class="addr-body">
                        {{ addr.province }} {{ addr.city }} {{ addr.town || addr.district }} <br/>
                        {{ addr.street || addr.detail }}
                      </div>
                      <div class="addr-actions">
                        <el-button link type="primary" icon="Edit" @click="openEditAddressDialog(addr)">编辑</el-button>
                        <el-button v-if="!addr.isDefault" link type="info" @click="handleSetDefault(addr)">设为默认</el-button>
                        <el-button link type="danger" icon="Delete" @click="handleDeleteAddress(addr.id)">删除</el-button>
                      </div>
                    </div>
                  </el-col>
                </el-row>
                <el-empty v-if="!loadingAddress && addresses.length === 0" description="暂无收货地址" />
              </div>

              <div v-else key="empty" class="tab-view"></div>

            </transition>
          </div>
        </el-col>
      </el-row>
    </div>
      
    <!-- 地址弹窗 (保持不变) -->
    <el-dialog 
      v-model="addressDialogVisible" 
      :title="isEditing ? '编辑地址' : '新增地址'" 
      width="500px"
      append-to-body
    >
      <el-form 
        :model="addressForm" 
        ref="addressFormRef"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区" prop="cascaderValue">
          <el-cascader 
            v-model="addressForm.cascaderValue" 
            :options="cityOptions" 
            placeholder="请选择省/市/区" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            rows="2" 
            placeholder="请输入详细地址信息" 
          />
        </el-form-item>
        <el-form-item label="地址标签">
          <el-input v-model="addressForm.tag" placeholder="例如：家、公司" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAddressForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  User, List, Wallet, Location, Search, Plus, Edit, Delete, ShoppingCart, Star,
  Trophy, Ticket, Timer, ArrowRight, StarFilled, Picture, ChatLineSquare, MoreFilled,  // ★★★ 新增图标引入 ★★★
  Loading, 
  CameraFilled ,
  Setting // ★★★ 新增图标
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage, type FormInstance, type FormRules , type UploadRequestOptions} from 'element-plus'
import { useUserStore } from '@/stores/useUserStore';
import { uploadFile } from "@/api/upload";
// ★★★ 新增：引入个人资料相关 API ★★★
import { 
  updateUserInfo, 
  updatePassword, 
  type UserBasicInfoDTO, 
  type PasswordDTO ,
  getUserInfo
} from '@/api/user';

// API Imports
import { queryMyCarts } from '@/api/cart';
import { 
  getAddressList as apiGetAddressList, 
  addAddress as apiAddAddress, 
  updateAddress as apiUpdateAddress, 
  deleteAddress as apiDeleteAddress 
} from '@/api/address';
import { getOrderList, deleteOrder, getOrderDetail } from '@/api/order';
import { addOrder } from '@/api/order';
import { getCollectList, deleteById } from '@/api/collect';
import { getMyComment, deleteMyComment, type CommentVO } from '@/api/comment';
import { itemApi } from '@/api/item'; 
import { confirmOrder } from '@/api/order';

// ================= 类型定义 =================
interface OrderDetailVO {
  id: string | number;
  itemId: string | number;
  num: number;
  name: string;
  price: number;
  image?: string;
  spec: Record<string, string>;
}

interface OrderVO {
  id: string | number;
  totalFee: number;
  paymentType: number;
  status: number;
  createTime?: string;
  details?: OrderDetailVO[];
}

// --- 基础配置 ---
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentTab = ref('dashboard')

// --- 用户数据 ---
const user = computed(() => {
  const info:any = userStore.userInfo
  return {
    id: info.userId,
    nickname: info.nickName ,
    avatar: info.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    balance: Number(info.balance) || 0,
    level: info.levelName,
    exp: info.exp || 0,
    nextLevelExp: 1000,
    expPercentage: (info.exp || 0) / 1000 * 100,
    points: info.points || 0,
    coupons: info.coupons || 0,
    favorites: info.favorites || 0,
    footprints: info.footprints || 0,
    currentPoints:info.currentPoints || 0
  }
})

// --- 导航逻辑 ---
const handleNavSelect = (key: string) => {
  currentTab.value = key
  router.push({ path: '/user', query: { tab: key } })
}

const handleCommand = (command: string, item: any) => {
  if (command === 'delete') {
    handleDeleteComment(item.id)
  }
}

// ================= ★★★ 新增：个人资料逻辑 ★★★ =================
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const updatingProfile = ref(false)
const updatingPassword = ref(false)
// ★★★ 新增：头像上传 Loading 状态 ★★★
const avatarLoading = ref(false); 

// ★★★ 新增：上传前校验逻辑 ★★★
const BeforeUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false; 
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false; 
  }
  return true; 
};

// ★★★ 新增：自定义上传请求逻辑 ★★★
const customUploadRequest = async (options: UploadRequestOptions) => {
  const { file } = options;
  avatarLoading.value = true;

  try {
    const res: any = await uploadFile(file as any); 
    // 兼容后端返回结构
    const uploadedUrl = res?.url || res?.data?.url;

    if (uploadedUrl) {
      profileForm.avatar = uploadedUrl; // 更新表单数据
      ElMessage.success("头像上传成功");
    } else {
      throw new Error("返回数据中未找到图片地址");
    }
  } catch (error: any) {
    console.error("上传失败详情:", error);
    ElMessage.error(error.message || "头像上传失败，请检查网络");
  } finally {
    avatarLoading.value = false;
  }
};

// 基本信息表单
const profileForm = reactive<UserBasicInfoDTO>({
  nickName: '',
  avatar: '',
  phone: '',
  email: '',
  birthday: null,
  gender: 2 // 0女 1男 2未知
})

// 密码表单
const passwordForm = reactive<PasswordDTO>({
  phone: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const profileRules = reactive<FormRules>({
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
})

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
})

// 初始化/重置 个人信息
const initProfileData = () => {
  const info = userStore.userInfo || {}
  profileForm.nickName = info.nickName || info.nickName || ''
  profileForm.avatar = info.avatar || ''
  profileForm.phone = info.phone ||  ''
  profileForm.email = info.email || ''
  profileForm.birthday = info.birthday || ''
  profileForm.gender = info.gender !== undefined ? Number(info.gender) : 2
  
  // 同时初始化修改密码表单中的手机号
  passwordForm.phone =  ''
}

const resetProfileForm = () => {
  initProfileData()
}

// 提交个人信息修改
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      updatingProfile.value = true
      try {
        await updateUserInfo(profileForm)
        ElMessage.success('个人信息修改成功')
        // 更新 Store
        await fetchUserInfo() 
      } catch (error: any) {
        ElMessage.error(error.message || '修改失败')
      } finally {
        updatingProfile.value = false
      }
    }
  })
}

// 提交密码修改
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      updatingPassword.value = true
      try {
        // 确保 phone 存在
        if(!passwordForm.phone) {
           passwordForm.phone = userStore.userInfo?.phone || userStore.userInfo?.phone || ''
        }
        
        await updatePassword(passwordForm)
        ElMessage.success('密码修改成功，请重新登录')
        
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error: any) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        updatingPassword.value = false
      }
    }
  })
}

// ================= 订单管理逻辑 =================
const rawOrderList = ref<OrderVO[]>([])
const loadingOrders = ref(false)
const orderStatus = ref('all')
const searchOrder = ref('')
const orderCounts = ref({ pay: 0, ship: 0, receive: 0 })

// 状态映射字典
const statusMap: Record<number, { text: string; type: string }> = {
  1: { text: '待付款', type: 'danger' },
  2: { text: '待发货', type: 'warning' },
  3: { text: '待收货', type: 'primary' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'info' },
  6: { text: '已评价', type: 'success' }
}

const handleConfirmOrder = (orderId: string | number) => {
  ElMessageBox.confirm('确认已收到商品？', '提示', {
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await confirmOrder(orderId)
      ElMessage.success('订单已确认收货')
      fetchOrders() 
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

const formatStatus = (status: number) => {
  return statusMap[status] || { text: '未知状态', type: 'info' }
}

const handleViewDetail = (orderId: string | number) => {
  router.push(`/order/${orderId}`)
}

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') return '0.00'
  const str = price.toString()
  const padded = str.padStart(3, '0')
  return `${padded.slice(0, -2)}.${padded.slice(-2)}`
}

// 获取订单列表
const fetchOrders = async () => {
  if (!user.value.id) return
  loadingOrders.value = true
  try {
    const res = await getOrderList(user.value.id)
    if (res.data) {
      rawOrderList.value = res.data
      orderCounts.value = {
        pay: rawOrderList.value.filter(o => o.status === 1).length,
        ship: rawOrderList.value.filter(o => o.status === 2).length,
        receive: rawOrderList.value.filter(o => o.status === 3).length
      }
    }
  } catch (error) {
    console.error('获取订单失败', error)
  } finally {
    loadingOrders.value = false
  }
}

// 计算属性：根据条件筛选订单列表
const filteredOrderList = computed(() => {
  let list = rawOrderList.value
  if (orderStatus.value !== 'all') {
    const statusFilter: Record<string, number> = { 'pay': 1, 'ship': 2, 'receive': 3 }
    const targetStatus = statusFilter[orderStatus.value]
    if (targetStatus) {
      list = list.filter(item => item.status === targetStatus)
    }
  }
  if (searchOrder.value) {
    const keyword = searchOrder.value.toLowerCase()
    list = list.filter(item => 
      String(item.id).includes(keyword) || 
      item.details?.some(d => d.name.toLowerCase().includes(keyword))
    )
  }
  return list.sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime())
})

// 计算属性：Dashboard 用的最近订单 (取前3条)
const recentOrders = computed(() => {
  return rawOrderList.value.slice(0, 3).map(order => {
    const firstItem = order.details && order.details.length > 0 ? order.details[0] : null
    const statusObj = formatStatus(order.status)
    return {
      id: order.id,
      title: firstItem ? firstItem.name : '未知商品',
      image: firstItem ? firstItem.image : '',
      price: (order.totalFee / 100).toFixed(2),
      statusText: statusObj.text,
      statusType: statusObj.type
    }
  })
})

// 删除订单
const handleDeleteOrder = (orderId: string | number) => {
  ElMessageBox.confirm('确认删除该订单吗？删除后不可恢复', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOrder(orderId)
      ElMessage.success('删除成功')
      fetchOrders() 
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

// 支付 Mock
const handlePay = (orderId: string | number) => {
  const targetOrder = rawOrderList.value.find(o => o.id === orderId);
  if (targetOrder) {
    router.push({
      path: '/pay',
      query: {
        orderId: targetOrder.id,
        amount: targetOrder.totalFee / 100 
      }
    });
  }
}

// ================= 购物车逻辑 =================
const cartItems:any = ref([])
const fetchCart = async () => {
  try {
    const res: any = await queryMyCarts();
    if(res && res.data) {
        cartItems.value = res.data;
    }
  } catch(e) { console.error(e) }
}

// ================= 收藏管理逻辑 =================
const favoriteList = ref<any[]>([])
const loadingFavorites = ref(false)
const {userInfo}:any = userStore
const fetchFavorites = async () => {
  loadingFavorites.value = true
  try {
    const res: any = await getCollectList()
    favoriteList.value = Array.isArray(res) ? res : (res.data || res.rows || [])
    if (userStore.userInfo) {
      userStore.updateUserInfo({ ...userInfo })
    }
  } catch (error) {
    console.error('获取收藏列表失败', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loadingFavorites.value = false
  }
}

const handleDeleteFavorite = (itemId: number) => {
  ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteById(itemId)
      ElMessage.success('已取消收藏')
      fetchFavorites()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const goToProductDetail = (itemId: number) => {
  if (itemId) {
    router.push(`/item/${itemId}`)
  } else {
    ElMessage.warning('商品已失效')
  }
}

// ================= 我的评价逻辑 =================
const commentList = ref<any[]>([]) 
const loadingComments = ref(false)

const formatSku = (skuStr: string) => {
  if (!skuStr) return '';
  try {
    const cleanStr = skuStr.replace(/[{"}]/g, '').replace(/"/g, '').replace(/:/g, ': ');
    return cleanStr;
  } catch (e) {
    return skuStr;
  }
}
const fetchComments = async () => {
  loadingComments.value = true
  try {
    const res: any = await getMyComment()
    const list = Array.isArray(res) ? res : (res.data || [])
    
    const enrichedList = await Promise.all(list.map(async (item: any) => {
      const targetId = item.itemId || item.goodsId || item.bizId;
      if (targetId) {
        try {
          const itemRes: any = await itemApi.getItemById(targetId);
          const productData = itemRes.data || itemRes;
          return {
            ...item,
            productName: productData.name || productData.title, 
            productImage: productData.image || productData.pic, 
            itemId: targetId 
          }
        } catch (e) {
          console.error(`商品 ${targetId} 信息获取失败`, e);
          return item;
        }
      }
      return item;
    }));

    commentList.value = enrichedList;

  } catch (error) {
    console.error(error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loadingComments.value = false
  }
}

const handleDeleteComment = (id: number | string) => {
  ElMessageBox.confirm('确定要删除这条评价吗？删除后无法恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMyComment(id)
      ElMessage.success('删除成功')
      fetchComments() 
    } catch (error) {
    }
  })
}

// ================= 地址管理逻辑 =================
const addresses:any = ref([])
const loadingAddress = ref(false)
const addressDialogVisible = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const addressFormRef = ref<FormInstance>()

interface AddressFormState {
  id?: number;
  name: string;
  phone: string;
  cascaderValue: string[];
  detail: string;
  tag: string;
  isDefault: boolean;
}

const addressForm = reactive<AddressFormState>({
  name: '',
  phone: '',
  cascaderValue: [],
  detail: '',
  tag: '',
  isDefault: false
})

const addressRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  cascaderValue: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
})

const cityOptions = [
  { value: '北京市', label: '北京市', children: [{ value: '北京市', label: '北京市', children: [{ value: '朝阳区', label: '朝阳区' }, { value: '海淀区', label: '海淀区' }] }] },
  { value: '上海市', label: '上海市', children: [{ value: '上海市', label: '上海市', children: [{ value: '浦东新区', label: '浦东新区' }] }] }
]

const loadAddresses = async () => {
  loadingAddress.value = true
  try {
    const res: any = await apiGetAddressList()
    addresses.value = res.data || []
  } catch (error) {
    console.error('加载地址失败:', error)
  } finally {
    loadingAddress.value = false
  }
}

const openAddAddressDialog = () => {
  isEditing.value = false
  addressForm.id = undefined
  addressForm.name = ''
  addressForm.phone = ''
  addressForm.cascaderValue = []
  addressForm.detail = ''
  addressForm.tag = ''
  addressForm.isDefault = false
  addressDialogVisible.value = true
}

const openEditAddressDialog = (addr: any) => {
  isEditing.value = true
  addressForm.id = addr.id
  addressForm.name = addr.contact || addr.name
  addressForm.phone = addr.mobile || addr.phone
  if (addr.province && addr.city) {
    addressForm.cascaderValue = [addr.province, addr.city, addr.town || addr.district || '']
  } else {
    addressForm.cascaderValue = []
  }
  addressForm.detail = addr.street || addr.detail
  addressForm.tag = addr.notes || addr.tag || ''
  addressForm.isDefault = (addr.isDefault === 1 || addr.isDefault === true)
  addressDialogVisible.value = true
}

const submitAddressForm = async () => {
  if (!addressFormRef.value) return
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const [province, city, town] = addressForm.cascaderValue
        const payload = {
          contact: addressForm.name,
          mobile: addressForm.phone,
          province, city, town,
          street: addressForm.detail,
          notes: addressForm.tag,
          isDefault: addressForm.isDefault ? 1 : 0
        }
        if (isEditing.value && addressForm.id) {
          await apiUpdateAddress({ id: addressForm.id, ...payload })
          ElMessage.success('地址修改成功')
        } else {
          await apiAddAddress(payload)
          ElMessage.success('地址添加成功')
        }
        addressDialogVisible.value = false
        loadAddresses()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDeleteAddress = (id: number) => {
  ElMessageBox.confirm('确定要删除这个地址吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteAddress(id)
      ElMessage.success('删除成功')
      loadAddresses()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSetDefault = async (addr: any) => {
  try {
    await apiUpdateAddress({ id: addr.id, isDefault: 1 })
    ElMessage.success('设置默认地址成功')
    loadAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

// ================= 钱包 Mock =================
const transactionType = ref('all')
const transactions = reactive([
  { id: 1, title: '账户充值', time: '2024-01-20 10:30', amount: 1000, type: 'recharge' },
  { id: 2, title: '购物消费', time: '2024-01-18 15:45', amount: -299, type: 'consume' }
])
const filteredTransactions = computed(() => {
  if (transactionType.value === 'all') return transactions
  return transactions.filter(item => item.type === transactionType.value)
})

// ================= 生命周期 & Watch =================
onMounted(() => {
  if (currentTab.value === 'address') loadAddresses()
  if (currentTab.value === 'cart') fetchCart()
  if (currentTab.value === 'favorites') fetchFavorites()
  if (currentTab.value === 'comments') fetchComments()
  if (currentTab.value === 'orders' || currentTab.value === 'dashboard') fetchOrders()
  // ★★★ 新增：初始化个人资料 ★★★
  if (currentTab.value === 'profile') initProfileData()
})

watch(() => route.query.tab, (tab) => {
  if (tab && typeof tab === 'string') currentTab.value = tab
}, { immediate: true })

watch(currentTab, (newTab) => {
  if (newTab === 'address') loadAddresses()
  if (newTab === 'cart') fetchCart()
  if (newTab === 'favorites') fetchFavorites()
  if (newTab === 'comments') fetchComments() 
  if (newTab === 'orders' || newTab === 'dashboard') fetchOrders()
  // ★★★ 新增：监听切换到个人资料 ★★★
  if (newTab === 'profile') initProfileData()
})

const fetchUserInfo = async () => {
  // ✅ 第一步：先从 store 获取 ID
  // 注意：要做好防空处理，使用 ?. 防止报错
  const currentId = userStore.userInfo?.userId ;

  // ✅ 第二步：如果 ID 不存在，或者 ID 是 "未知ID"，直接拦截，不发请求
  if (!currentId ) {
    console.warn('用户ID不存在,跳过获取用户信息');
    return; 
  }

  try {
    // ✅ 第三步：传入确实存在的 ID
    const res = await getUserInfo();
    userStore.updateUserInfo(res.data);
  } catch (error) {
    console.error('获取用户信息失败', error);
  }
}

onMounted(() => { 
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
$primary: #409eff;
$bg-color: #f5f7fa;
$card-bg: #ffffff;
$text-main: #303133;
$text-sub: #909399;

.user-center-page {
  background: $bg-color;
  min-height: calc(100vh - 64px);
  padding: 84px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.main-row {
  display: flex;
  flex-wrap: nowrap;
  gap: 24px;
}

/* 左侧侧边栏 */
.profile-sidebar {
  background: $card-bg;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
  margin-bottom: 20px;

  .profile-header {
    background: linear-gradient(135deg, #eef5fe 0%, #fff 100%);
    padding: 30px 20px;
    text-align: center;
    border-bottom: 1px solid #f0f0f0;

    .avatar-box {
      position: relative;
      width: 80px;
      margin: 0 auto 12px;
      .user-avatar { border: 4px solid #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
      .vip-badge {
        position: absolute; bottom: 0; right: -5px; width: 24px; height: 24px;
        filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
      }
    }
    .username { margin: 0; font-size: 18px; color: $text-main; font-weight: 700; }
    .user-id { font-size: 12px; color: $text-sub; margin-top: 4px; }
    
    .vip-progress-box {
      margin-top: 20px; text-align: left; background: rgba(255,255,255,0.6);
      padding: 12px; border-radius: 8px;
      .level-row { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 6px; font-weight: bold; color: #d48806; }
      .exp-text { font-size: 10px; color: $text-sub; margin-top: 6px; text-align: center; }
    }
  }

  .menu-list {
    padding: 16px 0;
    .user-nav-menu {
      border-right: none;
      .el-menu-item {
        padding: 16px 24px; height: auto; line-height: 24px; margin: 0 16px 10px; border-radius: 12px;
        &:hover { background: #f5f7fa; color: $primary; }
        &.is-active { background: #ecf5ff; color: $primary; font-weight: 500; }
        .menu-icon { margin-right: 12px; font-size: 18px; }
      }
    }
  }
}

/* 右侧内容 */
.content-panel {
  .panel-title { font-size: 18px; color: $text-main; margin: 0 0 20px; font-weight: 700; }
  
  .asset-card {
    background: linear-gradient(120deg, #409eff 0%, #2a5caa 100%);
    color: #fff; border-radius: 16px; padding: 24px; height: 180px;
    display: flex; flex-direction: column; justify-content: space-between;
    box-shadow: 0 10px 30px rgba(64, 158, 255, 0.3);
    .card-top { display: flex; justify-content: space-between; opacity: 0.9; }
    .card-num { font-size: 32px; font-weight: 700; margin-top: 10px; font-family: sans-serif; }
    .card-bottom { display: flex; gap: 10px; .glass-btn { background: rgba(255,255,255,0.2); color: #fff; border: 1px solid rgba(255,255,255,0.4); } }
  }

  .stats-grid {
    display: grid; grid-template-columns: 1fr 1fr; gap: 15px; height: 180px;
    .stat-box {
      background: #fff; border-radius: 12px; display: flex; flex-direction: column; justify-content: center; align-items: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.03);
      transition: all 0.3s;
      &:hover { transform: translateY(-3px); box-shadow: 0 8px 16px rgba(0,0,0,0.06); }
      .stat-icon { font-size: 24px; margin-bottom: 8px; }
      .val { font-size: 18px; font-weight: bold; color: $text-main; }
      .lbl { font-size: 12px; color: $text-sub; }
    }
  }
/* ... 原有样式保持不变 ... */

/* ★★★ 新增：创意头像上传样式 ★★★ */
.creative-avatar-wrapper {
  display: flex;
  justify-content: flex-start; /* 配合 form-item 对齐 */
  position: relative;
}

.avatar-uploader {
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.02);
  }
}

.avatar-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  
  &:hover {
    .trigger-circle {
      transform: translateY(-4px) scale(1.05);
      background: #fff;
      box-shadow: 0 12px 30px rgba(64, 158, 255, 0.3);
      border-color: #fff;
      
      &::after { opacity: 1; }
    }
    .camera-icon { color: #409eff; transform: scale(1.1); }
    .trigger-label { color: #409eff; transform: translateY(2px); }
  }
}

.trigger-circle {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(236, 245, 255, 0.6));
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.15), inset 0 0 15px rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  z-index: 1;

  &::after {
    content: "";
    position: absolute;
    top: -4px; left: -4px; right: -4px; bottom: -4px;
    border-radius: 50%;
    border: 1px dashed rgba(64, 158, 255, 0.4);
    animation: rotateCircle 10s linear infinite;
    opacity: 0;
    transition: opacity 0.3s;
  }
}

.camera-icon {
  color: #a0cfff;
  transition: all 0.3s;
}

.trigger-label {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  opacity: 0.8;
  transition: all 0.3s;
}

.avatar-preview-box {
  position: relative;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  padding: 3px;
  background: #fff;
  box-shadow: 0 10px 25px rgba(31, 38, 135, 0.15);
  cursor: pointer;
  transition: transform 0.3s;

  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    display: block;
  }

  .avatar-hover-mask {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    border-radius: 50%;
    background: rgba(23, 23, 23, 0.5);
    backdrop-filter: blur(2px);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    opacity: 0;
    transition: all 0.3s ease;
    font-size: 12px;
    
    .el-icon { margin-bottom: 2px; }
  }

  &:hover {
    transform: scale(1.02);
    .avatar-hover-mask { opacity: 1; }
  }
}

@keyframes rotateCircle {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
} 
  /* 订单列表样式 */
  .order-list {
    display: flex; flex-direction: column; gap: 20px; min-height: 300px;
  }

  .order-card {
    border-radius: 8px; border: 1px solid #ebeef5;
    .order-header {
      display: flex; justify-content: space-between; align-items: center; padding: 12px 20px;
      background: #f9fafe; border-bottom: 1px solid #ebeef5; font-size: 13px;
      .header-left { color: #909399; .order-time { margin-right: 15px; } }
    }
    .order-body { padding: 0 20px; }
    .order-item {
      display: flex; padding: 15px 0; border-bottom: 1px dashed #eee;
      &:last-child { border-bottom: none; }
      .item-img {
        width: 80px; height: 80px; border-radius: 6px; border: 1px solid #eee; margin-right: 15px; flex-shrink: 0;
        display: flex; align-items: center; justify-content: center; background: #fff;
      }
      .item-info {
        flex: 1;
        .item-name { font-size: 14px; color: #303133; margin-bottom: 8px; line-height: 1.4; }
        .item-spec { font-size: 12px; color: #909399; }
      }
      .item-price-num {
        text-align: right; margin-left: 20px;
        .price { font-size: 14px; color: #303133; font-weight: 500; }
        .num { font-size: 12px; color: #909399; margin-top: 4px; }
      }
    }
    .order-footer {
      padding: 12px 20px; border-top: 1px solid #ebeef5; display: flex; justify-content: space-between; align-items: center;
      .total-section {
        font-size: 13px; color: #606266;
        .total-price { font-size: 18px; color: #f56c6c; font-weight: bold; margin-left: 5px; }
      }
      .action-btns { display: flex; gap: 10px; }
    }
  }

  /* Dashboard 最近订单微调 */
  .mini-order-item {
    display: flex; align-items: center; padding: 10px; background: #fff; border-radius: 8px; margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.03); cursor: pointer; transition: all 0.2s;
    &:hover { transform: translateX(5px); }
    .mini-img { width: 50px; height: 50px; border-radius: 4px; margin-right: 10px; background: #f5f7fa; }
    .mini-info { flex: 1; .mini-title { font-size: 13px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; width: 150px; } }
    .mini-price { font-weight: bold; color: #303133; }
  }
  
  /* 购物车样式 */
  .cart-preview-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.02); }
  .cart-list-enhanced { display: flex; flex-direction: column; gap: 16px; max-height: 500px; overflow-y: auto; padding: 4px; }
  .cart-card {
    border: none; background: #f9fafe; transition: all 0.2s;
    &:hover { transform: translateX(4px); background: #f0f2f5; }
    .cart-item-flex {
      display: flex; align-items: center; gap: 16px;
      .cart-thumb { width: 60px; height: 60px; border-radius: 8px; flex-shrink: 0; background: #fff; border: 1px solid #eee; display: flex; align-items: center; justify-content: center; }
      .cart-details {
        flex: 1; display: flex; flex-direction: column; justify-content: center;
        .cart-title { font-size: 14px; font-weight: 500; color: $text-main; margin-bottom: 4px; line-height: 1.4; }
        .cart-specs { display: flex; }
      }
      .cart-price-col { text-align: right; .price { color: #f56c6c; font-weight: bold; font-size: 15px; } .qty { color: #909399; font-size: 12px; margin-top: 2px; } }
    }
  }
  .cart-action-bar {
    margin-top: 20px; border-top: 1px dashed #eee; padding-top: 20px; display: flex; justify-content: space-between; align-items: center;
    .total-info { font-size: 14px; color: #606266; span { color: $primary; font-weight: bold; font-size: 16px; margin: 0 2px; } }
  }

  /* 收藏卡片样式 */
  .fav-card {
    border: none;
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: #fff;
    &:hover { 
      transform: translateY(-5px); 
      box-shadow: 0 12px 24px rgba(0,0,0,0.12); 
      .fav-img-box .fav-img { transform: scale(1.08); }
      .fav-img-box .fav-overlay { opacity: 1; }
    }
    .fav-img-box {
      position: relative; height: 180px; cursor: pointer; overflow: hidden; 
      .fav-img { width: 100%; height: 100%; display: block; transition: transform 0.5s ease; }
      .fav-overlay {
        position: absolute; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(2px);
        display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 12px;
        opacity: 0; transition: opacity 0.3s;
      }
    }
    .fav-info {
      padding: 12px 14px;
      .fav-title { 
        font-size: 14px; color: #333; margin-bottom: 8px; line-height: 1.4; height: 40px; 
        overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
      }
      .fav-tags {
        display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; height: 20px; overflow: hidden;
        .tag-item {
          font-size: 10px; padding: 1px 5px; border-radius: 4px; border: 1px solid; line-height: 14px;
          &.blue { color: #409eff; border-color: rgba(64,158,255,0.3); background: rgba(64,158,255,0.05); }
          &.red { color: #f56c6c; border-color: rgba(245,108,108,0.3); background: rgba(245,108,108,0.05); }
          &.gray { color: #909399; border-color: #e9e9eb; background: #f4f4f5; }
        }
      }
      .fav-bottom { display: flex; justify-content: space-between; align-items: flex-end; border-top: 1px dashed #f0f0f0; padding-top: 8px; }
      .fav-price { color: #f56c6c; font-weight: 700; line-height: 1; }
      .fav-time { font-size: 11px; color: #c0c4cc; }
    }
  }

  /* 评价卡片样式 */
  .comment-list { display: flex; flex-direction: column; gap: 20px; }
  .dreamy-card {
    background: #ffffff; border-radius: 12px; padding: 20px; display: flex; gap: 20px; transition: all 0.3s ease;
    border: 1px solid #f0f2f5; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
    &:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06); }
    .card-left {
      flex-shrink: 0; width: 80px; height: 80px;
      .product-thumb { width: 100%; height: 100%; border-radius: 8px; border: 1px solid #eee; cursor: pointer; }
      .img-error-slot { width: 100%; height: 100%; background: #f9f9f9; }
    }
    .card-center {
      flex: 1; display: flex; flex-direction: column; min-width: 0;
      .user-info-row {
        display: flex; align-items: center; gap: 10px; margin-bottom: 10px;
        .mini-avatar { border: 1px solid #eee; }
        .user-meta {
          display: flex; flex-direction: column;
          .mini-nickname { font-size: 14px; font-weight: 600; color: #303133; line-height: 1.2; }
          .mini-rate { transform: scale(0.8); transform-origin: left center; height: 16px; }
        }
        .time { margin-left: auto; font-size: 12px; color: #c0c4cc; }
      }
      .content-text { font-size: 15px; color: #303133; line-height: 1.6; margin-bottom: 12px; white-space: pre-wrap; }
      .review-imgs {
        display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px;
        .review-img-item { width: 100px; height: 100px; border-radius: 8px; cursor: pointer; border: 1px solid #f0f0f0; transition: opacity 0.2s; &:hover { opacity: 0.9; } }
      }
      .merchant-reply-box {
        background: #f7f8fa; padding: 10px 12px; border-radius: 6px; font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 12px;
        .reply-label { color: #909399; font-weight: bold; }
      }
      .card-footer {
        display: flex; justify-content: space-between; align-items: center; padding-top: 10px; border-top: 1px dashed #f0f0f0;
        .footer-product {
          display: flex; align-items: center; gap: 6px; font-size: 12px; color: #909399; cursor: pointer; max-width: 80%;
          &:hover { color: $primary; }
          .product-link { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        }
        .sku-tag { background-color: #f6f6f6; border: none; color: #666; border-radius: 4px; padding: 0 6px; height: 20px; line-height: 20px; font-size: 11px; margin-left: 8px; transition: all 0.2s; }
        .footer-product:hover .sku-tag { background-color: #eee; }
        .footer-actions { .more-btn { cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s; &:hover { background: #f0f2f5; } } }
      }
    }
  }

  /* 地址卡片 */
  .address-card {
    background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 20px; position: relative;
    border: 1px solid #ebeef5; transition: all 0.3s;
    &:hover { border-color: $primary; box-shadow: 0 4px 16px rgba(0,0,0,0.05); }
    &.default { border: 1px solid rgba(64,158,255,0.4); background: #f0f9eb; }
    .addr-header { display: flex; align-items: center; margin-bottom: 10px; .name { font-weight: bold; margin-right: 10px; } .default-tag { margin-left: auto; } }
    .addr-body { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 15px; height: 40px; }
    .addr-actions { display: flex; justify-content: flex-end; border-top: 1px solid #eee; padding-top: 10px; }
  }
  
  /* 钱包交易记录 */
  .transaction-section { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid #ebeef5; margin-top: 20px; }
  .transaction-item {
    display: flex; justify-content: space-between; align-items: center; padding: 16px 0; border-bottom: 1px solid #f5f7fa;
    &:last-child { border-bottom: none; }
    .transaction-title { font-size: 14px; font-weight: 500; color: $text-main; }
    .transaction-time { font-size: 12px; color: $text-sub; margin-top: 4px; }
    .transaction-amount { font-size: 16px; font-weight: bold; &.recharge { color: #67c23a; } &.consume { color: #f56c6c; } }
  }

  /* ★★★ 新增：个人资料页样式 ★★★ */
  .profile-container {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  }

  .profile-tabs {
    border: none;
    box-shadow: none;
    
    :deep(.el-tabs__content) {
      padding: 30px;
    }
    
    :deep(.el-tabs__item) {
      height: 50px;
      line-height: 50px;
      font-size: 15px;
      
      &.is-active {
        color: $primary;
        font-weight: bold;
      }
    }
  }

  .profile-form, .password-form {
    max-width: 500px;
    margin-top: 10px;
  }

  .avatar-edit-box {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .avatar-input {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 5px;
      
      .tip {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

@media (max-width: 768px) {
  .main-row { flex-wrap: wrap; }
  .profile-sidebar { margin-bottom: 20px; }
  .dreamy-card {
    flex-direction: column; gap: 12px;
    .card-left { display: none; } 
    .review-imgs .review-img-item { width: 31%; height: auto; aspect-ratio: 1/1; }
  }
}
</style>