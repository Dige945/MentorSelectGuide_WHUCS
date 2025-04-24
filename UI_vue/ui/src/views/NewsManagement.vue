<template>
  <div class="news-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>新闻管理</span>
            <el-button type="primary" @click="handleAdd" class="add-btn">添加新闻</el-button>
          </div>
          <div class="header-right">
            <el-button type="success" @click="handleManageTags">管理标签</el-button>
          </div>
        </div>
      </template>

      <el-table :data="newsList" style="width: 100%" v-loading="loading">
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="label" label="标签" width="120">
          <template #default="scope">
            <span>{{ scope.row.label }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑新闻对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="50%"
      :before-close="handleDialogClose"
    >
      <el-form :model="newsForm" ref="newsFormRef" :rules="rules" label-width="80px">
        <el-form-item label="标签" prop="label">
          <el-select
            v-model="newsForm.label"
            placeholder="选择标签"
            class="tag-select"
            multiple
            filterable
            allow-create
            default-first-option
            :reserve-keyword="false"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="newsForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 管理标签对话框 -->
    <el-dialog
      title="管理标签"
      v-model="tagDialogVisible"
      width="30%"
    >
      <div class="tag-list">
        <el-tag
          v-for="tag in availableTags"
          :key="tag"
          closable
          :disable-transitions="false"
          @close="handleRemoveTag(tag)"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
      </div>
      <div class="input-new-tag">
        <el-input
          v-model="newTag"
          placeholder="输入新标签"
          @keyup.enter="handleAddTag"
        >
          <template #append>
            <el-button @click="handleAddTag">添加</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'NewsManagement',
  setup() {
    const store = useStore()
    const router = useRouter()
    const currentUser = computed(() => store.state.user)
    
    const newsList = ref([])
    const loading = ref(false)
    const dialogVisible = ref(false)
    const tagDialogVisible = ref(false)
    const dialogTitle = ref('添加新闻')
    const newsFormRef = ref(null)
    const isEdit = ref(false)
    const newTag = ref('')
    const availableTags = ref(['科研', '活动', '通知', '其他'])

    // 检查权限
    const hasNewsManagementPermission = computed(() => {
      const user = currentUser.value
      if (!user) return false
      
      // 获取新闻管理路由的配置
      const newsManagementRoute = router.options.routes.find(route => route.name === 'NewsManagement')
      if (!newsManagementRoute || !newsManagementRoute.meta || !newsManagementRoute.meta.allowedRoles) {
        return false
      }
      
      // 检查用户是否在允许的角色列表中
      return newsManagementRoute.meta.allowedRoles.includes(user.username)
    })

    // 如果没有权限，显示错误信息并返回
    if (!hasNewsManagementPermission.value) {
      ElMessage.error('您没有权限访问此页面')
      return {
        newsList: [],
        loading: false
      }
    }

    const newsForm = ref({
      id: '',
      content: '',
      label: []
    })

    const rules = {
      content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
      label: [{ required: true, message: '请选择至少一个标签', trigger: 'change', type: 'array' }]
    }

    // 获取标签类型
    const getTagType = (tag) => {
      const types = {
        '科研': '',
        '活动': 'success',
        '通知': 'warning',
        '其他': 'info'
      }
      return types[tag] || 'info'
    }

    // 管理标签
    const handleManageTags = () => {
      tagDialogVisible.value = true
    }

    // 处理标签变化并保存到本地存储
    const saveAvailableTags = () => {
      localStorage.setItem('availableTags', JSON.stringify(availableTags.value))
    }

    // 添加标签
    const handleAddTag = () => {
      const tag = newTag.value.trim()
      if (tag && !availableTags.value.includes(tag)) {
        availableTags.value.push(tag)
        saveAvailableTags()
        newTag.value = ''
      }
    }

    // 移除标签
    const handleRemoveTag = (tag) => {
      const index = availableTags.value.indexOf(tag)
      if (index > -1) {
        availableTags.value.splice(index, 1)
        saveAvailableTags()
      }
    }

    // 获取新闻列表
    const fetchNewsList = async () => {
      try {
        loading.value = true
        const response = await request.get('/news/getNewsList')
        if (response.code === '0') {
          // 按时间从新到旧排序
          newsList.value = response.data.sort((a, b) => {
            return new Date(b.createdAt) - new Date(a.createdAt)
          })
        } else {
          ElMessage.error(response.msg || '获取新闻列表失败')
        }
      } catch (error) {
        console.error('获取新闻列表失败:', error)
        ElMessage.error('获取新闻列表失败')
      } finally {
        loading.value = false
      }
    }

    // 添加新闻
    const handleAdd = () => {
      isEdit.value = false
      dialogTitle.value = '添加新闻'
      newsForm.value = {
        id: '',
        content: '',
        label: []
      }
      dialogVisible.value = true
    }

    // 编辑新闻
    const handleEdit = (row) => {
      isEdit.value = true
      dialogTitle.value = '编辑新闻'
      try {
        const labels = typeof row.label === 'string' ? JSON.parse(row.label) : row.label
        newsForm.value = {
          id: row.id,
          content: row.content,
          label: Array.isArray(labels) ? labels : []
        }
        dialogVisible.value = true
      } catch (error) {
        console.error('解析标签失败:', error)
        ElMessage.error('加载新闻数据失败')
      }
    }

    // 删除新闻
    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除这条新闻吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await request.delete(`/news/deleteNews/${row.id}`)
          if (response.code === '0') {
            ElMessage.success('删除成功')
            fetchNewsList()
          } else {
            ElMessage.error(response.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除新闻失败:', error)
          ElMessage.error('删除失败')
        }
      })
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!newsFormRef.value) return
      
      await newsFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            // 确保标签是数组
            const labels = Array.isArray(newsForm.value.label) ? newsForm.value.label : []
            
            const formData = {
              id: newsForm.value.id,
              content: newsForm.value.content,
              label: JSON.stringify(labels)
            }
            
            const url = isEdit.value ? '/news/update' : '/news/insert'
            const response = await request.post(url, formData)
            
            if (response.code === '0') {
              ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
              dialogVisible.value = false
              fetchNewsList()
            } else {
              ElMessage.error(response.msg || (isEdit.value ? '更新失败' : '添加失败'))
            }
          } catch (error) {
            console.error('操作失败:', error)
            ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
          }
        }
      })
    }

    // 关闭对话框
    const handleDialogClose = () => {
      dialogVisible.value = false
      if (newsFormRef.value) {
        newsFormRef.value.resetFields()
      }
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      if (typeof timestamp === 'string' && timestamp.includes('-')) {
        return timestamp.split(' ')[0]
      }
      const date = new Date(timestamp)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }

    // 初始化时从本地存储加载标签
    onMounted(() => {
      const savedTags = localStorage.getItem('availableTags')
      if (savedTags) {
        availableTags.value = JSON.parse(savedTags)
      }
      if (hasNewsManagementPermission.value) {
        fetchNewsList()
      }
    })

    return {
      newsList,
      loading,
      dialogVisible,
      tagDialogVisible,
      dialogTitle,
      newsForm,
      newsFormRef,
      rules,
      availableTags,
      newTag,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleDialogClose,
      handleManageTags,
      handleAddTag,
      handleRemoveTag,
      formatTime,
      getTagType,
      hasNewsManagementPermission
    }
  }
}
</script>

<style scoped>
.news-management {
  padding: 20px;
  max-width: 1400px;
  margin: 20px auto;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 25px;
}

.header-left span {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-table {
  margin: 15px 0;
  font-size: 15px;
}

.el-table .cell {
  padding: 15px;
  line-height: 1.8;
}

.el-dialog {
  width: 65%;
  border-radius: 8px;
}

.el-dialog__body {
  padding: 30px 40px;
}

.el-form-item {
  margin-bottom: 25px;
}

.el-form-item__label {
  font-size: 16px;
  padding-right: 20px;
}

.el-input__inner,
.el-textarea__inner {
  font-size: 15px;
  padding: 12px 15px;
}

.el-textarea__inner {
  min-height: 120px !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px 0;
}

.tag-select {
  width: 100%;
}

.tag-list {
  margin: 20px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  margin: 5px;
  font-size: 15px;
  padding: 8px 15px;
}

.input-new-tag {
  margin-top: 25px;
}

.el-button {
  font-size: 15px;
  padding: 12px 25px;
}

.operation-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.operation-buttons .el-button {
  padding: 10px 20px;
  font-size: 14px;
}

.el-table__body-wrapper {
  min-height: 600px;
}
</style> 