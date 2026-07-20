<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab">
      <el-tab-pane :label="'巡检计划 ' + planTotal" name="plans">
        <el-form :inline="true" :model="planQuery" @keyup.enter="reloadPlans">
          <el-form-item label="状态">
            <el-select v-model="planQuery.status" clearable placeholder="全部" style="width: 120px" @change="reloadPlans">
              <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="周期">
            <el-select v-model="planQuery.cycleType" clearable placeholder="全部" style="width: 120px" @change="reloadPlans">
              <el-option v-for="item in CYCLES" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item><el-button @click="reloadPlans">查询</el-button><el-button type="primary" @click="openPlan()">新建计划</el-button></el-form-item>
        </el-form>

        <el-table v-loading="planLoading" :data="plans">
          <el-table-column prop="planName" label="计划名称" min-width="170" />
          <el-table-column label="周期" width="90"><template #default="{ row }">{{ labelOf(CYCLES, row.cycleType) }}</template></el-table-column>
          <el-table-column label="对象" min-width="160"><template #default="{ row }">{{ labelOf(TARGETS, row.targetType) }} / {{ row.targetName }}</template></el-table-column>
          <el-table-column prop="inspector" label="默认巡检人" width="120" />
          <el-table-column label="巡检项" min-width="220"><template #default="{ row }">{{ row.items.join('、') }}</template></el-table-column>
          <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPlan(row)">编辑</el-button>
              <el-button v-if="row.status === 1" link type="success" @click="openGenerate(row)">按日期派发</el-button>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="togglePlan(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="planTotal > planQuery.size" class="pager" layout="total, prev, pager, next" :total="planTotal" :current-page="planQuery.page" :page-size="planQuery.size" @current-change="changePlanPage" />
      </el-tab-pane>

      <el-tab-pane :label="'巡检任务 ' + taskTotal" name="tasks">
        <el-form :inline="true" :model="taskQuery" @keyup.enter="reloadTasks">
          <el-form-item label="状态">
            <el-select v-model="taskQuery.status" clearable placeholder="全部" style="width: 130px" @change="reloadTasks">
              <el-option v-for="item in TASK_STATUS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划日期"><el-date-picker v-model="taskQuery.plannedDate" type="date" value-format="YYYY-MM-DD" clearable @change="reloadTasks" /></el-form-item>
          <el-form-item><el-button @click="reloadTasks">查询</el-button></el-form-item>
        </el-form>

        <el-table v-loading="taskLoading" :data="tasks">
          <el-table-column prop="taskNo" label="任务号" width="160" />
          <el-table-column prop="planName" label="计划" min-width="160" />
          <el-table-column label="对象" min-width="150"><template #default="{ row }">{{ labelOf(TARGETS, row.targetType) }} / {{ row.targetName }}</template></el-table-column>
          <el-table-column prop="inspector" label="巡检人" width="110" />
          <el-table-column prop="plannedDate" label="计划日期" width="115" />
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="tagType(row.status)">{{ labelOf(TASK_STATUS, row.status) }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" link type="primary" @click="openExecute(row)">执行</el-button>
              <el-button v-if="row.status === 3" link type="success" @click="openRectify(row)">确认整改</el-button>
              <el-button v-if="row.status === 1" link type="danger" @click="cancelTask(row)">取消</el-button>
              <el-button v-if="row.status !== 1" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="taskTotal > taskQuery.size" class="pager" layout="total, prev, pager, next" :total="taskTotal" :current-page="taskQuery.page" :page-size="taskQuery.size" @current-change="changeTaskPage" />
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="planVisible" :title="editingPlan ? '编辑巡检计划' : '新建巡检计划'" width="600px">
    <el-form ref="planRef" :model="planForm" :rules="planRules" label-width="95px">
      <el-form-item label="计划名称" prop="planName"><el-input v-model="planForm.planName" /></el-form-item>
      <el-form-item label="巡检周期" prop="cycleType"><el-select v-model="planForm.cycleType" style="width: 100%"><el-option v-for="item in CYCLES" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="对象类型" prop="targetType"><el-select v-model="planForm.targetType" style="width: 100%" @change="resetTarget"><el-option v-for="item in TARGETS" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="巡检对象" prop="targetId">
        <div class="target-selects">
          <el-alert v-if="editingPlan && planForm.targetId" :title="`当前：${editingPlan.targetName}`" type="info" :closable="false" show-icon />
          <el-select v-model="selectedBuilding" placeholder="选择楼栋" @change="onBuilding" style="width: 100%"><el-option v-for="item in buildings" :key="item.id" :label="item.buildingName" :value="item.id" /></el-select>
          <el-select v-if="planForm.targetType >= 2" v-model="selectedFloor" placeholder="选择楼层" :disabled="!selectedBuilding" @change="onFloor" style="width: 100%"><el-option v-for="item in floors" :key="item.id" :label="item.floorName || `${item.floorNumber}层`" :value="item.id" /></el-select>
          <el-select v-if="planForm.targetType === 3" v-model="planForm.targetId" placeholder="选择房间" :disabled="!selectedFloor" style="width: 100%"><el-option v-for="item in rooms" :key="item.id" :label="item.roomNumber" :value="item.id" /></el-select>
        </div>
      </el-form-item>
      <el-form-item label="默认巡检人" prop="inspector"><el-input v-model="planForm.inspector" /></el-form-item>
      <el-form-item label="巡检项" prop="items"><el-checkbox-group v-model="planForm.items"><el-checkbox v-for="item in inspectionItems" :key="item.label" :label="item.label">{{ item.label }}</el-checkbox></el-checkbox-group></el-form-item>
      <el-form-item label="备注"><el-input v-model="planForm.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="planVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="savePlan">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="generateVisible" title="按日期派发巡检任务" width="440px">
    <el-alert title="周期计划可按不同日期重复派发；同一计划日期只能派发一次" type="info" show-icon :closable="false" style="margin-bottom: 16px" />
    <el-form label-width="85px"><el-form-item label="计划日期"><el-date-picker v-model="generateForm.plannedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item><el-form-item label="巡检人"><el-input v-model="generateForm.inspector" placeholder="留空使用计划默认值" /></el-form-item></el-form>
    <template #footer><el-button @click="generateVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="generateTask">按日期派发</el-button></template>
  </el-dialog>

  <el-dialog v-model="executeVisible" title="执行巡检" width="650px">
    <el-table :data="executeItems" border><el-table-column prop="item" label="巡检项" width="160" /><el-table-column label="结果" width="180"><template #default="{ row }"><el-radio-group v-model="row.passed"><el-radio :value="true">正常</el-radio><el-radio :value="false">异常</el-radio></el-radio-group></template></el-table-column><el-table-column label="说明"><template #default="{ row }"><el-input v-model="row.note" :placeholder="row.passed ? '选填' : '异常时必填'" /></template></el-table-column></el-table>
    <template #footer><el-button @click="executeVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitExecution">提交</el-button></template>
  </el-dialog>

  <el-dialog v-model="rectifyVisible" title="确认整改" width="480px"><el-input v-model="rectificationNote" type="textarea" :rows="4" placeholder="填写整改结果" /><template #footer><el-button @click="rectifyVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitRectification">确认完成</el-button></template></el-dialog>

  <el-dialog v-model="detailVisible" title="巡检详情" width="620px">
    <el-descriptions v-if="currentTask" :column="2" border><el-descriptions-item label="任务号">{{ currentTask.taskNo }}</el-descriptions-item><el-descriptions-item label="状态">{{ labelOf(TASK_STATUS, currentTask.status) }}</el-descriptions-item><el-descriptions-item label="对象">{{ currentTask.targetName }}</el-descriptions-item><el-descriptions-item label="巡检人">{{ currentTask.inspector }}</el-descriptions-item><el-descriptions-item v-if="currentTask.rectificationNote" label="整改结果" :span="2">{{ currentTask.rectificationNote }}</el-descriptions-item></el-descriptions>
    <el-table v-if="currentTask" :data="currentTask.results" style="margin-top: 16px"><el-table-column prop="item" label="巡检项" /><el-table-column label="结果" width="100"><template #default="{ row }"><el-tag :type="row.passed ? 'success' : 'danger'">{{ row.passed ? '正常' : '异常' }}</el-tag></template></el-table-column><el-table-column prop="note" label="说明" /></el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings } from '@/api/building'
import { listFloors } from '@/api/floor'
import { pageRooms } from '@/api/room'
import type { Building, Floor, Room } from '@/api/types'
import { loadDictOptions, labelOf, type DictOption } from '@/utils/dict'
import {
  cancelInspectionTask, changeInspectionPlanStatus, createInspectionPlan, executeInspectionTask,
  generateInspectionTask, getInspectionTask, pageInspectionPlans, pageInspectionTasks,
  rectifyInspectionTask, updateInspectionPlan, type InspectionItemResult, type InspectionPlan,
  type InspectionTask, type PlanSave
} from '@/api/inspection'

const CYCLES = [{ value: 1, label: '日常' }, { value: 2, label: '每周' }, { value: 3, label: '每月' }, { value: 4, label: '每季度' }]
const TARGETS = [{ value: 1, label: '楼栋' }, { value: 2, label: '楼层' }, { value: 3, label: '房间' }]
const TASK_STATUS = [{ value: 1, label: '待执行' }, { value: 2, label: '检查通过' }, { value: 3, label: '待整改' }, { value: 4, label: '已整改' }, { value: 5, label: '已取消' }]

const activeTab = ref('plans')
const saving = ref(false)
const planLoading = ref(false)
const taskLoading = ref(false)
const plans = ref<InspectionPlan[]>([])
const tasks = ref<InspectionTask[]>([])
const planTotal = ref(0)
const taskTotal = ref(0)
const planQuery = reactive({ status: undefined as number | undefined, cycleType: undefined as number | undefined, page: 1, size: 10 })
const taskQuery = reactive({ status: undefined as number | undefined, plannedDate: undefined as string | undefined, page: 1, size: 10 })

const planVisible = ref(false)
const planRef = ref<FormInstance>()
const editingPlan = ref<InspectionPlan>()
const emptyPlan = (): PlanSave => ({ planName: '', cycleType: 1, targetType: 1, targetId: 0, inspector: '', items: [], remark: '' })
const planForm = reactive<PlanSave>(emptyPlan())
const planRules = { planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }], targetId: [{ required: true, message: '请选择巡检对象', trigger: 'change' }], inspector: [{ required: true, message: '请输入巡检人', trigger: 'blur' }], items: [{ type: 'array', required: true, min: 1, message: '至少选择一个巡检项', trigger: 'change' }] }
const inspectionItems = ref<DictOption[]>([])
const buildings = ref<Building[]>([])
const floors = ref<Floor[]>([])
const rooms = ref<Room[]>([])
const selectedBuilding = ref<number>()
const selectedFloor = ref<number>()

const generateVisible = ref(false)
const generatePlan = ref<InspectionPlan>()
const generateForm = reactive({ plannedDate: '', inspector: '' })
const executeVisible = ref(false)
const executeItems = ref<InspectionItemResult[]>([])
const currentTask = ref<InspectionTask>()
const rectifyVisible = ref(false)
const rectificationNote = ref('')
const detailVisible = ref(false)

function tagType(status: number) { return ({ 1: 'warning', 2: 'success', 3: 'danger', 4: 'success', 5: 'info' } as Record<number, string>)[status] as any }
async function reloadPlans() { planLoading.value = true; try { const res = await pageInspectionPlans(planQuery); plans.value = res.records; planTotal.value = res.total } finally { planLoading.value = false } }
async function reloadTasks() { taskLoading.value = true; try { const res = await pageInspectionTasks(taskQuery); tasks.value = res.records; taskTotal.value = res.total } finally { taskLoading.value = false } }
function changePlanPage(page: number) { planQuery.page = page; reloadPlans() }
function changeTaskPage(page: number) { taskQuery.page = page; reloadTasks() }

function openPlan(row?: InspectionPlan) {
  editingPlan.value = row
  Object.assign(planForm, row ? { planName: row.planName, cycleType: row.cycleType, targetType: row.targetType, targetId: row.targetId, inspector: row.inspector, items: [...row.items], remark: row.remark || '' } : emptyPlan())
  selectedBuilding.value = row?.targetType === 1 ? row.targetId : undefined
  selectedFloor.value = undefined; floors.value = []; rooms.value = []
  planVisible.value = true
}
function resetTarget() { planForm.targetId = 0; selectedBuilding.value = undefined; selectedFloor.value = undefined; floors.value = []; rooms.value = [] }
async function onBuilding(id: number) {
  selectedFloor.value = undefined; floors.value = []; rooms.value = []
  if (planForm.targetType === 1) planForm.targetId = id
  else { planForm.targetId = 0; floors.value = await listFloors(id) }
}
async function onFloor(id: number) {
  if (planForm.targetType === 2) planForm.targetId = id
  else { planForm.targetId = 0; rooms.value = (await pageRooms({ buildingId: selectedBuilding.value, floorId: id, page: 1, size: 100 })).records }
}
async function savePlan() {
  await planRef.value?.validate(); saving.value = true
  try { editingPlan.value ? await updateInspectionPlan(editingPlan.value.id, planForm) : await createInspectionPlan(planForm); ElMessage.success('已保存'); planVisible.value = false; reloadPlans() } finally { saving.value = false }
}
async function togglePlan(row: InspectionPlan) { await changeInspectionPlanStatus(row.id, row.status === 1 ? 0 : 1); ElMessage.success(row.status === 1 ? '已停用' : '已启用'); reloadPlans() }
function openGenerate(row: InspectionPlan) { generatePlan.value = row; generateForm.plannedDate = ''; generateForm.inspector = ''; generateVisible.value = true }
async function generateTask() {
  if (!generatePlan.value || !generateForm.plannedDate) return ElMessage.warning('请选择计划日期')
  saving.value = true; try { await generateInspectionTask(generatePlan.value.id, generateForm); ElMessage.success('已派发'); generateVisible.value = false; activeTab.value = 'tasks'; reloadTasks() } finally { saving.value = false }
}
async function openExecute(row: InspectionTask) { currentTask.value = await getInspectionTask(row.id); executeItems.value = currentTask.value.items.map(item => ({ item, passed: true, note: '' })); executeVisible.value = true }
async function submitExecution() {
  if (!currentTask.value) return
  if (executeItems.value.some(item => !item.passed && !item.note?.trim())) return ElMessage.warning('异常巡检项必须填写说明')
  saving.value = true; try { await executeInspectionTask(currentTask.value.id, executeItems.value); ElMessage.success('巡检结果已提交'); executeVisible.value = false; reloadTasks() } finally { saving.value = false }
}
function openRectify(row: InspectionTask) { currentTask.value = row; rectificationNote.value = ''; rectifyVisible.value = true }
async function submitRectification() {
  if (!currentTask.value || !rectificationNote.value.trim()) return ElMessage.warning('请填写整改结果')
  saving.value = true; try { await rectifyInspectionTask(currentTask.value.id, rectificationNote.value); ElMessage.success('整改已完成'); rectifyVisible.value = false; reloadTasks() } finally { saving.value = false }
}
async function cancelTask(row: InspectionTask) { await ElMessageBox.confirm('确认取消该巡检任务？', '提示', { type: 'warning' }); await cancelInspectionTask(row.id); ElMessage.success('已取消'); reloadTasks() }
async function openDetail(row: InspectionTask) { currentTask.value = await getInspectionTask(row.id); detailVisible.value = true }

onMounted(async () => { const [buildingPage, items] = await Promise.all([pageBuildings({ page: 1, size: 100 }), loadDictOptions('INSPECTION_ITEM')]); buildings.value = buildingPage.records; inspectionItems.value = items; await Promise.all([reloadPlans(), reloadTasks()]) })
</script>

<style scoped>
.pager { margin-top: 12px; justify-content: flex-end; }
.target-selects { display: grid; gap: 8px; width: 100%; }
:deep(.el-tabs__header) { margin-bottom: 18px; }
:deep(.el-tabs__item) { height: 44px; font-weight: 600; }
@media (max-width: 767px) { :deep(.el-tabs__nav-wrap) { padding: 0 4px; } :deep(.el-dialog__body) { overflow-x: auto; } .target-selects { min-width: 0; } }
</style>
