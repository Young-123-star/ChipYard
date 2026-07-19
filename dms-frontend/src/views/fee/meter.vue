<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-form :inline="true">
        <el-form-item label="电价(元/度)"><el-input-number v-model="rate.electricityPrice" :min="0" :precision="4" :step="0.0001" /></el-form-item>
        <el-form-item label="水价(元/吨)"><el-input-number v-model="rate.waterPrice" :min="0" :precision="4" :step="0.0001" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saveRate">保存单价</el-button></el-form-item>
      </el-form>
      <el-button @click="router.push('/rooms')">配置房间账户</el-button>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="结算账户" name="accounts">
        <el-alert v-if="accounts.some(x => !x.configured)" title="存在配置不完整的账户，请在房间管理中统一账户编码和计算规则" type="warning" show-icon :closable="false" class="notice" />
        <el-table :data="accounts" border>
          <el-table-column prop="buildingId" label="楼栋ID" width="90" />
          <el-table-column prop="accountCode" label="账户编码" min-width="130" />
          <el-table-column label="方式" width="90"><template #default="{ row }">{{ row.settlementMode === 1 ? '户级' : '房间' }}</template></el-table-column>
          <el-table-column label="包含房间" min-width="180"><template #default="{ row }">{{ row.roomNumbers.join('、') }}</template></el-table-column>
          <el-table-column label="用电规则" min-width="130"><template #default="{ row }">{{ electricRuleLabel(row.electricityRule) }}</template></el-table-column>
          <el-table-column label="用水规则" min-width="130"><template #default="{ row }">{{ waterRuleLabel(row.waterRule) }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.configured ? 'success' : 'danger'">{{ row.configured ? '可结算' : '需修正' }}</el-tag></template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="抄表台账" name="readings">
        <el-form :inline="true">
          <el-form-item label="账期"><el-date-picker v-model="period" type="month" value-format="YYYY-MM" style="width: 140px" @change="loadReadings" /></el-form-item>
          <el-form-item><el-button @click="loadReadings">查询</el-button><el-button type="primary" @click="openReading">录入抄表</el-button></el-form-item>
        </el-form>
        <el-table :data="readings" border>
          <el-table-column prop="accountCode" label="账户" width="130" />
          <el-table-column label="表位" min-width="150"><template #default="{ row }">{{ row.targetType === 1 ? '户总表' : ('房间 ' + roomNumber(row.roomId)) }}</template></el-table-column>
          <el-table-column label="类型" width="90"><template #default="{ row }">{{ meterLabel(row.meterType) }}</template></el-table-column>
          <el-table-column prop="period" label="账期" width="100" />
          <el-table-column prop="prevReading" label="上期读数" />
          <el-table-column prop="currentReading" label="本期读数" />
          <el-table-column prop="consumption" label="用量" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="月度结算" name="settlements">
        <el-form :inline="true">
          <el-form-item label="账期"><el-date-picker v-model="period" type="month" value-format="YYYY-MM" style="width: 140px" /></el-form-item>
          <el-form-item><el-button @click="onPreview">结算预览</el-button><el-button type="primary" @click="onGenerate">确认生成</el-button></el-form-item>
        </el-form>
        <template v-if="preview">
          <el-alert v-if="!preview.valid" :title="'预览未通过：' + preview.errors.join('；')" type="error" show-icon :closable="false" class="notice" />
          <el-alert v-else :title="`结算周期 ${preview.cycleStart} 至 ${preview.cycleEnd}，校验通过`" type="success" show-icon :closable="false" class="notice" />
          <el-table :data="preview.accounts" border class="notice">
            <el-table-column prop="accountCode" label="账户" />
            <el-table-column prop="electricityUsage" label="用电量" />
            <el-table-column prop="waterUsage" label="用水量" />
            <el-table-column label="实际费用"><template #default="{ row }">¥{{ money(row.totalCost) }}</template></el-table-column>
            <el-table-column label="员工承担"><template #default="{ row }">¥{{ money(row.employeeAmount) }}</template></el-table-column>
            <el-table-column label="公司承担"><template #default="{ row }">¥{{ money(row.companyAmount) }}</template></el-table-column>
          </el-table>
        </template>
        <h3 class="section-title">已生成结算</h3>
        <el-table :data="settlements" border>
          <el-table-column prop="period" label="账期" width="100" />
          <el-table-column prop="accountCode" label="账户" />
          <el-table-column label="周期" min-width="190"><template #default="{ row }">{{ row.cycleStart }} 至 {{ row.cycleEnd }}</template></el-table-column>
          <el-table-column label="员工承担"><template #default="{ row }">¥{{ money(row.employeeAmount) }}</template></el-table-column>
          <el-table-column label="公司承担"><template #default="{ row }">¥{{ money(row.companyAmount) }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '有效' : '已作废' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="90"><template #default="{ row }"><el-button v-if="row.status === 1" link type="danger" @click="onVoid(row.id)">作废</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="readingVisible" title="录入抄表" width="500px">
      <el-form ref="readingRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="结算账户" prop="accountKey">
          <el-select v-model="form.accountKey" style="width: 100%" @change="onAccountChange">
            <el-option v-for="item in accounts" :key="keyOf(item)" :label="`${item.accountCode}（${item.roomNumbers.join('、')}）`" :value="keyOf(item)" />
          </el-select>
        </el-form-item>
        <el-form-item label="表位" prop="targetType">
          <el-radio-group v-model="form.targetType" @change="onTargetChange">
            <el-radio :value="1" :disabled="selected?.settlementMode !== 1">户总表</el-radio><el-radio :value="2">房间表</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="房间" prop="roomId">
          <el-select v-model="form.roomId" :disabled="form.targetType === 1" style="width: 100%">
            <el-option v-for="id in selected?.roomIds || []" :key="id" :label="roomNumber(id)" :value="id" />
          </el-select>
        </el-form-item>
        <el-form-item label="表类型" prop="meterType">
          <el-select v-model="form.meterType" style="width: 100%"><el-option label="电表" :value="1" /><el-option label="冷水表" :value="2" /><el-option label="热水表" :value="3" /></el-select>
        </el-form-item>
        <el-form-item label="账期" prop="period"><el-date-picker v-model="form.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item>
        <el-form-item label="上期读数"><el-input-number v-model="form.prevReading" :min="0" :precision="2" style="width: 100%" /><div class="hint">首次录入该表时必填，之后自动取上期</div></el-form-item>
        <el-form-item label="本期读数" prop="currentReading"><el-input-number v-model="form.currentReading" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="readingVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="onSaveReading">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { generateUtilitySettlement, getUtilityRate, listUtilityAccounts, listUtilityReadings, listUtilitySettlements, previewUtilitySettlement, saveUtilityReading, updateUtilityRate, voidUtilitySettlement } from '@/api/fee'
import { pageRooms } from '@/api/room'
import type { MeterReading, Room, UtilityAccount, UtilityPreview, UtilitySettlement } from '@/api/types'

const router = useRouter()
const tab = ref('accounts')
const period = ref(new Date().toISOString().slice(0, 7))
const saving = ref(false)
const accounts = ref<UtilityAccount[]>([])
const readings = ref<MeterReading[]>([])
const settlements = ref<UtilitySettlement[]>([])
const rooms = ref<Room[]>([])
const preview = ref<UtilityPreview>()
const rate = reactive({ electricityPrice: 0, waterPrice: 0 })
const readingVisible = ref(false)
const readingRef = ref<FormInstance>()
const form = reactive<{ accountKey?: string; buildingId?: number; accountCode?: string; targetType?: number; roomId?: number; meterType?: number; period?: string; prevReading?: number; currentReading?: number }>({})
const rules = {
  accountKey: [{ required: true, message: '请选择结算账户', trigger: 'change' }],
  targetType: [{ required: true, message: '请选择表位', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  meterType: [{ required: true, message: '请选择表类型', trigger: 'change' }],
  period: [{ required: true, message: '请选择账期', trigger: 'change' }],
  currentReading: [{ required: true, message: '请输入本期读数', trigger: 'blur' }]
}
const selected = computed(() => accounts.value.find(item => keyOf(item) === form.accountKey))

function keyOf(item: UtilityAccount) { return `${item.buildingId}|${item.accountCode}` }
function roomNumber(id: number) { return rooms.value.find(item => item.id === id)?.roomNumber || id }
function meterLabel(type: number) { return ({ 1: '电表', 2: '冷水表', 3: '热水表' } as Record<number, string>)[type] || type }
function electricRuleLabel(rule: number) { return ({ 0: '不计', 1: '户级250度', 2: '房间250度', 3: '夫妻平摊', 4: '公司承担' } as Record<number, string>)[rule] }
function waterRuleLabel(rule: number) { return ({ 0: '不计', 1: '户级50吨', 2: '房间17吨', 3: '夫妻平摊', 4: '公司承担' } as Record<number, string>)[rule] }
function money(value: number) { return Number(value || 0).toFixed(2) }

async function loadBase() {
  const [a, r, p] = await Promise.all([listUtilityAccounts(), pageRooms({ page: 1, size: 1000 }), getUtilityRate()])
  accounts.value = a; rooms.value = r.records
  rate.electricityPrice = Number(p.electricityPrice); rate.waterPrice = Number(p.waterPrice)
}
async function loadReadings() { readings.value = await listUtilityReadings({ period: period.value }) }
async function loadSettlements() { settlements.value = await listUtilitySettlements(period.value) }
async function saveRate() { await updateUtilityRate(rate); ElMessage.success('单价已保存') }
function openReading() {
  Object.assign(form, { accountKey: undefined, buildingId: undefined, accountCode: undefined, targetType: 2, roomId: undefined, meterType: 1, period: period.value, prevReading: undefined, currentReading: undefined })
  readingVisible.value = true
}
function onAccountChange() {
  form.buildingId = selected.value?.buildingId; form.accountCode = selected.value?.accountCode
  if (selected.value?.settlementMode !== 1 && form.targetType === 1) form.targetType = 2
  onTargetChange()
}
function onTargetChange() { form.roomId = selected.value?.roomIds[0] }
async function onSaveReading() {
  await readingRef.value?.validate(); saving.value = true
  try {
    await saveUtilityReading(form as any); ElMessage.success('抄表已保存'); readingVisible.value = false; await loadReadings()
  } finally { saving.value = false }
}
async function onPreview() { if (period.value) preview.value = await previewUtilitySettlement(period.value) }
async function onGenerate() {
  if (!period.value) return
  const result = await generateUtilitySettlement(period.value)
  ElMessage.success(`生成 ${result.settlements} 个结算、${result.bills} 张个人账单`)
  preview.value = undefined; await Promise.all([loadReadings(), loadSettlements()])
}
async function onVoid(id: number) {
  await ElMessageBox.confirm('确认作废该结算及其未缴个人账单？', '提示', { type: 'warning' })
  await voidUtilitySettlement(id); ElMessage.success('结算已作废，可修改抄表后重新生成'); await loadSettlements()
}
onMounted(async () => { await loadBase(); await Promise.all([loadReadings(), loadSettlements()]) })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; }
.notice { margin-bottom: 14px; }
.section-title { margin: 22px 0 10px; font-size: 15px; }
.hint { color: var(--el-text-color-secondary); font-size: 12px; }
</style>