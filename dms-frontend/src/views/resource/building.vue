<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="楼栋名称">
          <el-input v-model="query.buildingName" placeholder="名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in BUILDING_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button type="success" @click="openCreate">新增</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="loading" class="cards">
        <div v-for="b in list" :key="b.id" class="bld-card">
          <div class="bld-head">
            <div>
              <span class="bld-name">{{ b.buildingName }}</span>
              <span class="bld-code">{{ b.buildingCode }}</span>
            </div>
            <el-tag :type="tagTypeOf(BUILDING_STATUS, b.status) as any" size="small" round>{{ labelOf(BUILDING_STATUS, b.status) }}</el-tag>
          </div>
          <div class="bld-addr">{{ b.address || '—' }}</div>

          <div class="bld-body">
            <el-progress
              type="dashboard"
              :width="92"
              :stroke-width="8"
              :percentage="rate(b)"
              :color="rateColor(rate(b))"
            >
              <template #default>
                <span class="ring-num">{{ rate(b) }}%</span>
                <span class="ring-label">入住率</span>
              </template>
            </el-progress>
            <div class="bld-stats">
              <div><span>楼层</span><b>{{ b.floorCount }}</b></div>
              <div><span>房间</span><b>{{ b.realRoomCount ?? 0 }}</b></div>
              <div><span>床位</span><b>{{ b.realBedCount ?? 0 }}</b></div>
              <div><span>空闲床位</span><b class="free">{{ (b.realBedCount ?? 0) - (b.occupiedBeds ?? 0) }}</b></div>
              <div><span>电梯</span><b>{{ b.hasElevator === 1 ? '有' : '无' }}</b></div>
            </div>
          </div>

          <div class="bld-foot">
            <el-button link type="primary" @click="goRooms(b)">查看房间</el-button>
            <el-button link type="primary" @click="openEdit(b)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(b)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && !list.length" description="暂无楼栋" />
      </div>

      <el-pagination
        v-if="total > query.size"
        style="margin-top: 12px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :total="total"
        :current-page="query.page"
        :page-size="query.size"
        @current-change="onPageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼栋' : '新增楼栋'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="编码" prop="buildingCode">
          <el-input v-model="form.buildingCode" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" prop="buildingName"><el-input v-model="form.buildingName" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="楼层数" prop="floorCount"><el-input-number v-model="form.floorCount" :min="1" /></el-form-item>
        <el-form-item label="电梯">
          <el-switch v-model="form.hasElevator" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="s in BUILDING_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings, createBuilding, updateBuilding, deleteBuilding } from '@/api/building'
import type { Building } from '@/api/types'
import { BUILDING_STATUS, labelOf, tagTypeOf } from '@/utils/dict'

const router = useRouter()

function rate(b: Building): number {
  const beds = b.realBedCount ?? 0
  return beds ? Math.round(((b.occupiedBeds ?? 0) / beds) * 100) : 0
}
function rateColor(pct: number): string {
  if (pct >= 90) return '#f56c6c'
  if (pct >= 60) return '#e6a23c'
  return '#34c759'
}
function goRooms(b: Building) {
  router.push({ path: '/rooms', query: { buildingId: String(b.id) } })
}

const loading = ref(false)
const saving = ref(false)
const list = ref<Building[]>([])
const total = ref(0)
const query = reactive({ buildingName: '', status: undefined as number | undefined, page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Building>>({})
const rules = {
  buildingCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  buildingName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  floorCount: [{ required: true, message: '请输入楼层数', trigger: 'blur' }]
}

async function reload() {
  loading.value = true
  try {
    const res = await pageBuildings(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  query.page = p
  reload()
}

function openCreate() {
  Object.assign(form, { id: undefined, buildingCode: '', buildingName: '', address: '', floorCount: 1, hasElevator: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function openEdit(row: Building) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateBuilding(form.id, form)
    } else {
      await createBuilding(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Building) {
  await ElMessageBox.confirm(`确认删除楼栋「${row.buildingName}」？`, '提示', { type: 'warning' })
  await deleteBuilding(row.id)
  ElMessage.success('删除成功')
  reload()
}

onMounted(reload)
</script>

<style scoped>
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 8px;
}
.bld-card {
  border: 1px solid var(--dms-hairline);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  padding: 18px 20px;
  transition: box-shadow 0.18s, transform 0.18s;
}
.bld-card:hover { box-shadow: 0 8px 28px rgba(29, 79, 138, 0.1); transform: translateY(-2px); }
.bld-head { display: flex; align-items: center; justify-content: space-between; }
.bld-name { font-size: 17px; font-weight: 700; letter-spacing: -0.01em; }
.bld-code { font-size: 12px; color: var(--dms-ink-2); margin-left: 8px; }
.bld-addr { font-size: 12.5px; color: var(--dms-ink-2); margin-top: 4px; }
.bld-body { display: flex; align-items: center; gap: 20px; margin: 14px 0 6px; }
.ring-num { font-size: 18px; font-weight: 700; color: var(--dms-ink); display: block; }
.ring-label { font-size: 11px; color: var(--dms-ink-2); }
.bld-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 18px; flex: 1; }
.bld-stats div { font-size: 12.5px; color: var(--dms-ink-2); display: flex; justify-content: space-between; }
.bld-stats b { color: var(--dms-ink); font-weight: 600; }
.bld-stats b.free { color: #1d8a3e; }
.bld-foot {
  display: flex; justify-content: flex-end; gap: 4px;
  border-top: 1px solid var(--dms-hairline); padding-top: 10px; margin-top: 8px;
}
</style>
