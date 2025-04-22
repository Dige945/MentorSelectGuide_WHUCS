<template>
  <div class="forum-container">
    <!-- 帖子列表区域 -->
    <div class="posts-section">
      <el-card class="post-list">
        <template #header>
          <div class="post-header">
            <h2>论坛帖子</h2>
            <el-button 
              type="primary" 
              @click="showCreatePostDialog"
              class="create-post-btn"
            >发布新帖</el-button>
          </div>
        </template>
        
        <!-- 帖子列表 -->
        <div class="posts-wrapper">
          <transition-group name="post-fade">
            <div v-for="post in posts" 
                 :key="post.postId" 
                 class="post-item"
                 @click="viewPost(post)">
              <el-card class="post-card" :body-style="{ padding: '0px' }">
                <div class="post-content">
                  <div class="post-category-tag">
                    {{ getCategoryLabel(post.categoryId) }}
                  </div>
                  <div class="post-main">
                    <h3>{{ post.title }}</h3>
                    <div class="post-meta">
                      <span><i class="el-icon-chat-dot-round"></i> 回复 {{ post.replyCount || 0 }}</span>
                      <span><i class="el-icon-star-off"></i> 点赞 {{ post.likeCount || 0 }}</span>
                      <span class="post-date">{{ formatDate(post.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </transition-group>
        </div>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
            layout="prev, pager, next"
            background
          />
        </div>
      </el-card>
    </div>

    <!-- 发帖对话框 -->
    <el-dialog
      v-model="createPostDialogVisible"
      title="发布新帖"
      width="600px"
      class="post-dialog"
    >
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="newPost.title" placeholder="请输入标题"/>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            placeholder="请输入内容"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="newPost.categoryId" placeholder="请选择分类">
            <el-option label="导师咨询" :value="1"/>
            <el-option label="学习交流" :value="2"/>
            <el-option label="生活分享" :value="3"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createPostDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createPost">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog
      v-model="postDetailDialogVisible"
      :title="currentPost.title"
      width="700px"
      class="detail-dialog"
    >
      <div class="post-detail">
        <div class="post-author">
          <el-avatar :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <div class="author-info">
            <span class="author-name">{{ currentPost.authorName }}</span>
            <span class="post-time">{{ formatDate(currentPost.createdAt) }}</span>
          </div>
        </div>
        <div class="post-main-content">{{ currentPost.content }}</div>
        <div class="post-actions">
          <el-button 
            type="text" 
            @click="likePost(currentPost.postId)" 
            class="action-btn"
            :disabled="hasLiked(currentPost.postId)"
          >
            <i :class="[hasLiked(currentPost.postId) ? 'el-icon-star-on' : 'el-icon-star-off']"></i>
            <span>点赞 {{ currentPost.likeCount }}</span>
          </el-button>
          <el-button type="text" class="action-btn">
            <i class="el-icon-chat-dot-round"></i>
            <span>回复 {{ replies.length }}</span>
          </el-button>
        </div>
      </div>

      <!-- 回复列表 -->
      <div class="replies-section">
        <div class="replies-header">
          <h4>全部回复 ({{ replies.length }})</h4>
          <el-select v-model="replySort" size="small" class="sort-select">
            <el-option label="最新回复" value="newest" />
            <el-option label="最早回复" value="oldest" />
          </el-select>
        </div>
        
        <div class="replies-list">
          <transition-group name="reply-fade">
            <div v-for="(reply, index) in replies" :key="reply.postId" class="reply-item">
              <div class="floor-tag">{{ index + 1 }}楼</div>
              <div class="reply-author">
                <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <div class="author-info">
                  <span class="author-name">{{ reply.authorName }}</span>
                  <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                </div>
              </div>
              <div class="reply-main-content">{{ reply.content }}</div>
              <div class="reply-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  class="action-btn"
                  @click="likeReply(reply.postId)"
                  :disabled="hasLiked(reply.postId)"
                >
                  <i :class="[hasLiked(reply.postId) ? 'el-icon-star-on' : 'el-icon-star-off']"></i>
                  <span>点赞 {{ reply.likeCount || 0 }}</span>
                </el-button>
              </div>
            </div>
          </transition-group>
        </div>
      </div>

      <!-- 发表回复 -->
      <div class="reply-form">
        <div class="reply-form-header">
          <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <span>发表回复</span>
        </div>
        <el-input
          v-model="newReply.content"
          type="textarea"
          :rows="3"
          placeholder="写下你的回复..."
          resize="none"
          class="reply-input"
        />
        <div class="reply-form-footer">
          <el-button type="text" @click="postDetailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :disabled="!newReply.content.trim()">
            发表回复
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ForumView',
  data() {
    return {
      posts: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      createPostDialogVisible: false,
      postDetailDialogVisible: false,
      newPost: {
        title: '',
        content: '',
        categoryId: null
      },
      currentPost: {},
      replies: [],
      newReply: {
        content: ''
      },
      replySort: 'newest',
      likedPosts: new Set(JSON.parse(localStorage.getItem('likedPosts') || '[]'))
    }
  },
  methods: {
    // 获取帖子列表
    async fetchPosts() {
      this.loading = true;
      try {
        const params = new URLSearchParams({
          pageNum: this.currentPage,
          pageSize: this.pageSize
        });
        
        if (this.selectedCategory) {
          params.append('categoryId', this.selectedCategory);
        }

        const response = await fetch(`/api/forum/posts?${params}`);
        const data = await response.json();
        
        if (data.code === '0') {
          this.posts = data.data.records;
          this.total = data.data.total;
        } else {
          this.$message.error(data.msg || '获取帖子列表失败');
        }
      } catch (error) {
        console.error('获取帖子列表错误:', error);
        this.$message.error('获取帖子列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 创建新帖子
    async createPost() {
      if (!this.newPost.title.trim() || !this.newPost.content.trim() || !this.newPost.categoryId) {
        this.$message.warning('请填写完整的帖子信息')
        return
      }

      try {
        const response = await fetch('/api/forum/posts', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.newPost)
        })
        const data = await response.json()
        
        if (data.code === '0') {
          this.$message.success('发布成功')
          this.createPostDialogVisible = false
          // 重置表单
          this.newPost = { 
            title: '', 
            content: '', 
            categoryId: null 
          }
          // 等待后端处理完成
          await new Promise(resolve => setTimeout(resolve, 300))
          // 重置到第一页并重新获取列表
          this.currentPage = 1
          await this.fetchPosts()
        } else {
          this.$message.error(data.msg || '发布失败')
        }
      } catch (error) {
        console.error('发布帖子错误:', error)
        this.$message.error('发布失败')
      }
    },

    // 查看帖子详情
    async viewPost(post) {
      if (!post || !post.postId) {
        console.error('查看帖子详情失败: 无效的帖子数据')
        return
      }

      this.currentPost = post
      this.postDetailDialogVisible = true
      await this.fetchReplies(post.postId)
      await this.updatePostReplyCount(post.postId)
    },

    // 获取回复列表
    async fetchReplies(postId) {
      try {
        const response = await fetch(`/api/forum/posts/replies/${postId}`)
        const data = await response.json()
        if (data.code === '0') {
          this.replies = data.data
        } else {
          this.$message.error('获取回复列表失败')
        }
      } catch (error) {
        console.error('获取回复列表错误:', error)
        this.$message.error('获取回复列表失败')
      }
    },

    // 提交回复
    async submitReply() {
      if (!this.newReply.content.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }

      const reply = {
        parentId: this.currentPost.postId,
        content: this.newReply.content,
        categoryId: this.currentPost.categoryId
      }

      try {
        const response = await fetch('/api/forum/posts', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(reply)
        })
        const data = await response.json()
        if (data.code === '0') {
          this.$message.success('回复成功')
          this.newReply.content = ''
          await this.fetchReplies(this.currentPost.postId)
          // 更新回复数
          await this.updatePostReplyCount(this.currentPost.postId)
        } else {
          this.$message.error(data.msg || '回复失败')
        }
      } catch (error) {
        console.error('提交回复错误:', error)
        this.$message.error('回复失败')
      }
    },

    // 更新单个帖子的回复数
    async updatePostReplyCount(postId) {
      if (!postId) {
        console.error('更新回复数失败: postId 未定义')
        return
      }

      try {
        const replyCountResponse = await fetch(`/api/forum/posts/${postId}/reply-count`)
        const replyCountData = await replyCountResponse.json()
        if (replyCountData.code === '0') {
          // 更新当前帖子的回复数
          if (this.currentPost && this.currentPost.postId === postId) {
            this.currentPost.replyCount = replyCountData.data
          }
          // 更新列表中对应帖子的回复数
          const postInList = this.posts.find(p => p.postId === postId)
          if (postInList) {
            postInList.replyCount = replyCountData.data
          }
        }
      } catch (error) {
        console.error(`更新帖子 ${postId} 回复数失败:`, error)
      }
    },

    // 检查是否已经点赞
    hasLiked(postId) {
      return this.likedPosts.has(postId)
    },

    // 点赞帖子
    async likePost(postId) {
      if (this.hasLiked(postId)) return
      
      try {
        const response = await fetch(`/api/forum/posts/${postId}/like`, {
          method: 'POST'
        })
        const data = await response.json()
        
        if (data.code === '0') {
          this.currentPost.likeCount++
          this.likedPosts.add(postId)
          localStorage.setItem('likedPosts', JSON.stringify([...this.likedPosts]))
          this.$message.success('点赞成功')
        } else {
          this.$message.error(data.msg || '点赞失败')
        }
      } catch (error) {
        console.error('点赞错误:', error)
        this.$message.error('点赞失败')
      }
    },

    // 点赞回复
    async likeReply(replyId) {
      if (this.hasLiked(replyId)) return
      
      try {
        const response = await fetch(`/api/forum/posts/${replyId}/like`, {
          method: 'POST'
        })
        const data = await response.json()
        
        if (data.code === '0') {
          const reply = this.replies.find(r => r.postId === replyId)
          if (reply) {
            reply.likeCount = (reply.likeCount || 0) + 1
          }
          this.likedPosts.add(replyId)
          localStorage.setItem('likedPosts', JSON.stringify([...this.likedPosts]))
          this.$message.success('点赞成功')
        } else {
          this.$message.error(data.msg || '点赞失败')
        }
      } catch (error) {
        console.error('点赞错误:', error)
        this.$message.error('点赞失败')
      }
    },

    // 显示发帖对话框
    showCreatePostDialog() {
      this.createPostDialogVisible = true
    },

    // 处理分页变化
    async handlePageChange(page) {
      this.currentPage = page
      await this.fetchPosts()
    },

    // 监听分类变化
    async handleCategoryChange() {
      this.currentPage = 1
      await this.fetchPosts()
    },

    // 格式化日期
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    getCategoryLabel(categoryId) {
      const categories = {
        1: '导师咨询',
        2: '学习交流',
        3: '生活分享'
      }
      return categories[categoryId] || '其他'
    }
  },
  mounted() {
    this.fetchPosts()
  }
}
</script>

<style scoped>
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.posts-section {
  margin-top: 20px;
}

.post-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.post-header h2 {
  margin: 0;
  font-weight: 500;
  color: #333;
}

.posts-wrapper {
  width: 800px;
  margin: 0 auto;
}

.post-item {
  margin-bottom: 16px;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.post-item:hover {
  transform: translateY(-2px);
}

.post-card {
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.post-content {
  display: flex;
  align-items: flex-start;
  padding: 16px;
}

.post-category-tag {
  min-width: 80px;
  padding: 4px 8px;
  text-align: center;
  background-color: #f0f2f5;
  color: #409EFF;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  margin-right: 16px;
  transition: all 0.3s ease;
}

.post-item:hover .post-category-tag {
  background-color: #409EFF;
  color: white;
}

.post-main {
  flex: 1;
}

.post-main h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.post-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 14px;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  transition: all 0.3s ease;
}

.action-btn:disabled {
  color: #409EFF;
  cursor: not-allowed;
}

.action-btn:not(:disabled):hover {
  color: #409EFF;
  transform: translateY(-1px);
}

.el-icon-star-on {
  color: #409EFF;
}

.post-actions {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.reply-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  justify-content: flex-end;
}

/* 回复列表样式 */
.replies-section {
  margin-top: 24px;
}

.replies-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.replies-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.sort-select {
  width: 120px;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  position: relative;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.reply-item:hover {
  transform: translateX(4px);
  border-color: #ddd;
}

.reply-author {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.reply-main-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin: 12px 0;
  padding-left: 44px;
}

.reply-actions {
  display: flex;
  gap: 12px;
  padding-left: 44px;
  justify-content: flex-end;
}

/* 回复表单样式 */
.reply-form {
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.reply-form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #333;
  font-size: 15px;
}

.reply-input {
  margin-bottom: 16px;
}

.reply-input :deep(.el-textarea__inner) {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
}

.reply-input :deep(.el-textarea__inner:focus) {
  border-color: #333;
}

.reply-form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画效果 */
.post-fade-enter-active,
.post-fade-leave-active {
  transition: all 0.3s ease;
}

.post-fade-enter-from,
.post-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.reply-fade-enter-active,
.reply-fade-leave-active {
  transition: all 0.3s ease;
}

.reply-fade-enter-from,
.reply-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 对话框样式优化 */
.detail-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    margin: 0;
    border-bottom: 1px solid #eee;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 500;
  }
}

/* 创建帖子按钮 */
.create-post-btn {
  background: #333;
  border-color: #333;
  transition: all 0.3s ease;
}

.create-post-btn:hover {
  background: #000;
  border-color: #000;
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .posts-wrapper {
    width: 100%;
  }
}

.floor-tag {
  position: absolute;
  right: 16px;
  top: 16px;
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.post-main-content {
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  margin: 20px 0;
  padding: 0 10px;
  letter-spacing: 0.5px;
}

.reply-main-content {
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
  font-size: 15px;
  line-height: 1.7;
  color: #2c3e50;
  margin: 12px 0;
  padding: 0 10px;
  letter-spacing: 0.3px;
}

.author-name {
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.post-time, .reply-time {
  font-size: 12px;
  color: #909399;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 12px;
}

/* 优化帖子和回复的整体样式 */
.post-detail {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.reply-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  position: relative;
}

.floor-tag {
  position: absolute;
  right: 16px;
  top: 16px;
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

/* 优化作者信息布局 */
.post-author, .reply-author {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
</style>
