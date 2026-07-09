<template>
  <div class="dict-page">
    <el-card shadow="never" class="type-panel">
      <template #header>
        <div class="panel-head">
          <span>字典类型</span>
          <el-button type="primary" size="small" @click="openTypeDialog()">新增类型</el-button>
        </div>
      </template>
      <el-table :data="types" v-loading="typeLoading" highlight-current-row @current-change="onTypeSelect">
        <el-table-column prop="dictName" label="名称" min-width="120" />
        <el-table-column prop="dictType" label="编码" min-width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openTypeDialog(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.systemFlag === 1" @click.stop="removeType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="item-panel">
      <template #header>
        <div class="panel-head">
          <span>{{ currentType?.dictName || '字典项' }}</span>
          <el-button type="primary" size="small" :disabled="!currentType" @click="openItemDialog()">新增字典项</el-button>
        </div>
      </template>
      <el-table :data="items" v-loading="itemLoading" border>
        <el-table-column prop="dictLabel" label="显示名称" min-width="140" />
        <el-table-column prop="dictValue" label="值" min-width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="颜色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tagType || 'info'" size="small">{{ row.tagType || 'info' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="系统项" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.systemFlag === 1" type="warning" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openItemDialog(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.systemFlag === 1" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="typeDialog" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="460px">
      <el-form :model="typeForm" label-width="90px">
        <el-form-item label="类型编码" required><el-input v-model="typeForm.dictType" :disabled="typeForm.systemFlag === 1" /></el-form-item>
        <el-form-item label="类型名称" required><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="typeForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialog" :title="itemForm.id ? '编辑字典项' : '新增字典项'" width="520px">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="所属类型"><el-input v-model="itemForm.dictType" disabled /></el-form-item>
        <el-form-item label="显示名称" required><el-input v-model="itemForm.dictLabel" /></el-form-item>
        <el-form-item label="值" required><el-input v-model="itemForm.dictValue" :disabled="itemForm.systemFlag === 1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="颜色">
          <el-select v-model="itemForm.tagType" clearable style="width: 100%">
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
            <el-option label="info" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="itemForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="itemForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  listDictItems,
  listDictTypes,
  updateDictItem,
  updateDictType,
  type DictItem,
  type DictType
} from '@/api/dict'
import { clearDictCache } from '@/utils/dict'

const types = ref<DictType[]>([])
const items = ref<DictItem[]>([])
const currentType = ref<DictType>()
const typeLoading = ref(false)
const itemLoading = ref(false)
const saving = ref(false)
const typeDialog = ref(false)
const itemDialog = ref(false)

const typeForm = reactive<Partial<DictType>>({})
const itemForm = reactive<Partial<DictItem>>({})

async function loadTypes() {
  typeLoading.value = true
  try {
    types.value = await listDictTypes()
    if (!currentType.value && types.value.length) {
      await onTypeSelect(types.value[0])
    } else if (currentType.value) {
      const matched = types.value.find((type) => type.id === currentType.value?.id)
      if (matched) currentType.value = matched
    }
  } finally {
    typeLoading.value = false
  }
}

async function loadItems() {
  if (!currentType.value) return
  itemLoading.value = true
  try {
    items.value = await listDictItems(currentType.value.dictType, false)
  } finally {
    itemLoading.value = false
  }
}

async function onTypeSelect(row?: DictType) {
  if (!row) return
  currentType.value = row
  await loadItems()
}

function openTypeDialog(row?: DictType) {
  Object.assign(typeForm, row || { id: undefined, dictType: '', dictName: '', sortOrder: 0, status: 1, systemFlag: 0, remark: '' })
  typeDialog.value = true
}

async function saveType() {
  if (!typeForm.dictType || !typeForm.dictName) {
    ElMessage.warning('请填写类型编码和类型名称')
    return
  }
  saving.value = true
  try {
    const payload = { dictType: typeForm.dictType, dictName: typeForm.dictName, sortOrder: typeForm.sortOrder, status: typeForm.status, remark: typeForm.remark }
    if (typeForm.id) await updateDictType(typeForm.id, payload)
    else await createDictType(payload)
    ElMessage.success('保存成功')
    typeDialog.value = false
    await loadTypes()
  } finally {
    saving.value = false
  }
}

async function removeType(row: DictType) {
  await ElMessageBox.confirm(`确认删除字典类型“${row.dictName}”？`, '提示', { type: 'warning' })
  await deleteDictType(row.id)
  if (currentType.value?.id === row.id) currentType.value = undefined
  ElMessage.success('删除成功')
  await loadTypes()
}

function openItemDialog(row?: DictItem) {
  if (!currentType.value) return
  Object.assign(itemForm, row || { id: undefined, dictType: currentType.value.dictType, dictValue: '', dictLabel: '', sortOrder: 0, tagType: 'info', status: 1, systemFlag: 0, remark: '' })
  itemDialog.value = true
}

async function saveItem() {
  if (!itemForm.dictType || !itemForm.dictValue || !itemForm.dictLabel) {
    ElMessage.warning('请填写显示名称和值')
    return
  }
  saving.value = true
  try {
    const payload = {
      dictType: itemForm.dictType,
      dictValue: itemForm.dictValue,
      dictLabel: itemForm.dictLabel,
      sortOrder: itemForm.sortOrder,
      tagType: itemForm.tagType,
      status: itemForm.status,
      remark: itemForm.remark
    }
    if (itemForm.id) await updateDictItem(itemForm.id, payload)
    else await createDictItem(payload)
    clearDictCache(itemForm.dictType)
    ElMessage.success('保存成功')
    itemDialog.value = false
    await loadItems()
  } finally {
    saving.value = false
  }
}

async function removeItem(row: DictItem) {
  await ElMessageBox.confirm(`确认删除字典项“${row.dictLabel}”？`, '提示', { type: 'warning' })
  await deleteDictItem(row.id)
  clearDictCache(row.dictType)
  ElMessage.success('删除成功')
  await loadItems()
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-page {
  display: grid;
  grid-template-columns: minmax(380px, 0.9fr) minmax(520px, 1.4fr);
  gap: 16px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
}
.type-panel,
.item-panel {
  min-height: 520px;
}
@media (max-width: 1100px) {
  .dict-page { grid-template-columns: 1fr; }
}
</style>