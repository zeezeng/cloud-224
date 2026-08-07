<template>
  <div class="banner-crop-upload">
    <input
      ref="fileInputRef"
      class="native-file-input"
      type="file"
      accept="image/*"
      @change="handleFileChange"
    />

    <div v-if="modelValue" class="banner-preview">
      <n-image
        :src="modelValue"
        width="260"
        height="82"
        object-fit="cover"
        class="banner-preview-image"
      />
      <n-space>
        <n-button size="small" @click="openFilePicker">重新裁剪</n-button>
        <n-button size="small" type="error" ghost @click="handleRemove">移除</n-button>
      </n-space>
    </div>

    <button v-else class="crop-trigger" type="button" @click="openFilePicker">
      <span class="crop-trigger-title">选择图片并裁剪</span>
      <span class="crop-trigger-desc">比例 750:238，适配 uniapp 首页轮播</span>
    </button>

    <n-modal
      v-model:show="cropVisible"
      preset="card"
      title="裁剪轮播图"
      style="width: 880px; max-width: 94vw"
      :mask-closable="!uploading"
      @after-leave="resetCropState"
    >
      <div class="crop-panel">
        <canvas
          ref="canvasRef"
          class="crop-canvas"
          :width="cropWidth"
          :height="cropHeight"
          @pointerdown="handlePointerDown"
          @pointermove="handlePointerMove"
          @pointerup="handlePointerUp"
          @pointerleave="handlePointerUp"
        />
        <div class="crop-meta">
          <span>拖动图片调整位置</span>
          <span>输出 {{ cropWidth }} × {{ cropHeight }}</span>
        </div>
        <div class="zoom-row">
          <span class="zoom-label">缩放</span>
          <n-slider
            v-model:value="zoom"
            :min="1"
            :max="3"
            :step="0.01"
            @update:value="drawCrop"
          />
        </div>
      </div>

      <template #footer>
        <n-space justify="end">
          <n-button :disabled="uploading" @click="cropVisible = false">取消</n-button>
          <n-button type="primary" :loading="uploading" @click="uploadCroppedImage">确认裁剪并上传</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { fileApi } from '@/api/system'

defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | undefined): void
}>()

const message = useMessage()

const cropWidth = 1500
const cropHeight = 476
const fileInputRef = ref<HTMLInputElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const cropVisible = ref(false)
const uploading = ref(false)
const zoom = ref(1)

const sourceFile = ref<File | null>(null)
const imageEl = ref<HTMLImageElement | null>(null)
const imageUrl = ref('')
const baseScale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const dragging = ref(false)
const lastPointerX = ref(0)
const lastPointerY = ref(0)

watch(zoom, () => {
  constrainOffset()
  drawCrop()
})

function openFilePicker() {
  fileInputRef.value?.click()
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''
  if (!file) {
    return
  }
  if (!file.type.startsWith('image/')) {
    message.warning('请选择图片文件')
    return
  }
  loadImageForCrop(file)
}

function loadImageForCrop(file: File) {
  revokeObjectUrl()
  sourceFile.value = file
  const url = URL.createObjectURL(file)
  imageUrl.value = url

  const img = new Image()
  img.onload = async () => {
    imageEl.value = img
    baseScale.value = Math.max(cropWidth / img.naturalWidth, cropHeight / img.naturalHeight)
    zoom.value = 1
    offsetX.value = 0
    offsetY.value = 0
    cropVisible.value = true
    await nextTick()
    drawCrop()
  }
  img.onerror = () => {
    message.error('图片读取失败，请重新选择')
    revokeObjectUrl()
  }
  img.src = url
}

function drawCrop() {
  const canvas = canvasRef.value
  const img = imageEl.value
  if (!canvas || !img) {
    return
  }

  constrainOffset()
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return
  }

  const scale = baseScale.value * zoom.value
  const drawWidth = img.naturalWidth * scale
  const drawHeight = img.naturalHeight * scale
  const drawX = (cropWidth - drawWidth) / 2 + offsetX.value
  const drawY = (cropHeight - drawHeight) / 2 + offsetY.value

  ctx.clearRect(0, 0, cropWidth, cropHeight)
  ctx.fillStyle = '#111827'
  ctx.fillRect(0, 0, cropWidth, cropHeight)
  ctx.imageSmoothingEnabled = true
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(img, drawX, drawY, drawWidth, drawHeight)
}

function constrainOffset() {
  const img = imageEl.value
  if (!img) {
    return
  }

  const scale = baseScale.value * zoom.value
  const drawWidth = img.naturalWidth * scale
  const drawHeight = img.naturalHeight * scale
  const maxOffsetX = Math.max(0, (drawWidth - cropWidth) / 2)
  const maxOffsetY = Math.max(0, (drawHeight - cropHeight) / 2)

  offsetX.value = Math.min(maxOffsetX, Math.max(-maxOffsetX, offsetX.value))
  offsetY.value = Math.min(maxOffsetY, Math.max(-maxOffsetY, offsetY.value))
}

function handlePointerDown(event: PointerEvent) {
  if (!imageEl.value) {
    return
  }
  dragging.value = true
  lastPointerX.value = event.clientX
  lastPointerY.value = event.clientY
  canvasRef.value?.setPointerCapture(event.pointerId)
}

function handlePointerMove(event: PointerEvent) {
  if (!dragging.value || !canvasRef.value) {
    return
  }

  const rect = canvasRef.value.getBoundingClientRect()
  const scaleX = cropWidth / rect.width
  const scaleY = cropHeight / rect.height
  offsetX.value += (event.clientX - lastPointerX.value) * scaleX
  offsetY.value += (event.clientY - lastPointerY.value) * scaleY
  lastPointerX.value = event.clientX
  lastPointerY.value = event.clientY
  drawCrop()
}

function handlePointerUp(event: PointerEvent) {
  if (!dragging.value) {
    return
  }
  dragging.value = false
  canvasRef.value?.releasePointerCapture(event.pointerId)
}

async function uploadCroppedImage() {
  const canvas = canvasRef.value
  const file = sourceFile.value
  if (!canvas || !file) {
    return
  }

  uploading.value = true
  try {
    const blob = await canvasToBlob(canvas)
    const croppedFile = new File([blob], getCroppedFileName(file.name), { type: 'image/jpeg' })
    const result = await fileApi.uploadImage(croppedFile)
    emit('update:modelValue', result.url || result.filePath)
    cropVisible.value = false
    message.success('图片裁剪上传成功')
  } finally {
    uploading.value = false
  }
}

function canvasToBlob(canvas: HTMLCanvasElement) {
  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        resolve(blob)
      } else {
        reject(new Error('图片裁剪失败'))
      }
    }, 'image/jpeg', 0.92)
  })
}

function getCroppedFileName(fileName: string) {
  const name = fileName.replace(/\.[^.]+$/, '') || 'banner'
  return `${name}-banner.jpg`
}

function handleRemove() {
  emit('update:modelValue', undefined)
}

function resetCropState() {
  sourceFile.value = null
  imageEl.value = null
  zoom.value = 1
  offsetX.value = 0
  offsetY.value = 0
  dragging.value = false
  revokeObjectUrl()
}

function revokeObjectUrl() {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
    imageUrl.value = ''
  }
}
</script>

<style scoped>
.banner-crop-upload {
  width: 100%;
}

.native-file-input {
  display: none;
}

.banner-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.banner-preview-image {
  overflow: hidden;
  border: 1px solid var(--n-border-color);
  border-radius: 8px;
}

.crop-trigger {
  display: flex;
  width: 260px;
  height: 82px;
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--n-border-color);
  border-radius: 8px;
  background: var(--n-color);
  color: var(--n-text-color);
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.crop-trigger:hover {
  border-color: var(--n-primary-color);
  color: var(--n-primary-color);
  background: var(--n-color-hover);
}

.crop-trigger-title {
  font-size: 14px;
  font-weight: 600;
}

.crop-trigger-desc {
  margin-top: 6px;
  color: var(--n-text-color-3);
  font-size: 12px;
}

.crop-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.crop-canvas {
  width: 100%;
  aspect-ratio: 750 / 238;
  cursor: grab;
  border-radius: 10px;
  background: #111827;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.14);
  touch-action: none;
}

.crop-canvas:active {
  cursor: grabbing;
}

.crop-meta {
  display: flex;
  justify-content: space-between;
  color: var(--n-text-color-3);
  font-size: 12px;
}

.zoom-row {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
}

.zoom-label {
  color: var(--n-text-color-2);
  font-size: 13px;
}
</style>
