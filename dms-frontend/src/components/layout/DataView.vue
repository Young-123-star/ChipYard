<template>
  <section class="data-view">
    <div v-if="$slots.filters || $slots['filter-actions']" class="data-view__filters">
      <div class="data-view__fields"><slot name="filters" /></div>
      <div class="data-view__filter-actions"><slot name="filter-actions" /></div>
    </div>
    <div class="data-view__toolbar">
      <div>
        <h2>{{ title }}</h2>
        <span v-if="total !== undefined" class="data-view__count">共 {{ total }} 条</span>
      </div>
      <div v-if="$slots.actions" class="data-view__actions"><slot name="actions" /></div>
    </div>
    <div class="data-view__content"><slot /></div>
    <div v-if="$slots.pagination" class="data-view__pagination"><slot name="pagination" /></div>
  </section>
</template>

<script setup lang="ts">
defineProps<{ title: string; total?: number }>()
</script>

<style scoped>
.data-view { overflow: hidden; border: 1px solid var(--dms-hairline); border-radius: var(--dms-radius-card); background: var(--dms-surface); box-shadow: var(--dms-shadow-card); }
.data-view__filters { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; padding: 18px 20px; border-bottom: 1px solid var(--dms-hairline); background: #fbfcfe; }
.data-view__fields { display: flex; flex: 1; flex-wrap: wrap; align-items: flex-end; gap: 12px; min-width: 0; }
.data-view__filter-actions, .data-view__actions { display: flex; flex-wrap: wrap; gap: 8px; }
.data-view__toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 62px; padding: 0 20px; }
.data-view__toolbar > div:first-child { display: flex; align-items: baseline; gap: 10px; }
h2 { margin: 0; font-size: 16px; letter-spacing: -.015em; }
.data-view__count { color: var(--dms-ink-2); font-size: 13px; font-variant-numeric: tabular-nums; }
.data-view__content { min-width: 0; padding: 0 20px 20px; }
.data-view__pagination { display: flex; justify-content: flex-end; padding: 0 20px 20px; }
@media (max-width: 767px) {
  .data-view__filters { align-items: stretch; flex-direction: column; padding: 16px; }
  .data-view__filter-actions { justify-content: flex-end; }
  .data-view__toolbar { padding: 0 16px; }
  .data-view__content { overflow-x: auto; padding: 0 16px 16px; }
  .data-view__pagination { overflow-x: auto; justify-content: flex-start; padding: 0 16px 16px; }
}
</style>
