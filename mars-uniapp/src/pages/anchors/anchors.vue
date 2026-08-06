<script setup lang="ts">
import { anchorFilters, ephoneAnchors } from '@/data/ephone'
import AnchorCard from '@/components/ephone/AnchorCard.vue'
import CapsuleTabs from '@/components/ephone/CapsuleTabs.vue'
import YunPage from '@/components/ephone/YunPage.vue'

defineOptions({
  name: 'Anchors',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '主播',
  },
})
</script>

<template>
  <YunPage title="主播资料" subtitle="看见努力 · 记录成长">
    <CapsuleTabs :items="anchorFilters" />
    <AnchorCard
      v-for="(anchor, index) in ephoneAnchors"
      :key="anchor.id"
      :anchor="anchor"
      :rank="index + 1"
    />
  </YunPage>
</template>
