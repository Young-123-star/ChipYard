<template>
  <section class="app-page">
    <header class="app-page__header">
      <div class="app-page__copy">
        <span v-if="eyebrow" class="app-page__eyebrow">{{ eyebrow }}</span>
        <h1>{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="app-page__actions"><slot name="actions" /></div>
    </header>
    <div v-if="$slots.summary" class="app-page__summary"><slot name="summary" /></div>
    <slot />
  </section>
</template>

<script setup lang="ts">
defineProps<{ title: string; description?: string; eyebrow?: string }>()
</script>

<style scoped>
.app-page { display: grid; gap: 20px; }
.app-page__header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; min-height: 64px; }
.app-page__copy { min-width: 0; }
.app-page__eyebrow { display: block; margin-bottom: 6px; color: var(--dms-accent); font-size: 12px; font-weight: 700; letter-spacing: .09em; text-transform: uppercase; }
h1 { margin: 0; color: var(--dms-ink); font-size: 28px; line-height: 1.2; letter-spacing: -.035em; }
p { max-width: 720px; margin: 7px 0 0; color: var(--dms-ink-2); font-size: 14px; line-height: 1.6; }
.app-page__actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 10px; }
.app-page__summary { min-width: 0; }
@media (max-width: 767px) {
  .app-page { gap: 16px; }
  .app-page__header { align-items: stretch; flex-direction: column; gap: 14px; }
  .app-page__actions { justify-content: flex-start; }
  h1 { font-size: 24px; }
}
</style>
