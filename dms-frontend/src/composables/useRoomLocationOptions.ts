import { ref } from 'vue'
import { pageBuildings } from '@/api/building'
import { listFloors } from '@/api/floor'
import { pageRooms } from '@/api/room'
import type { Building, Floor, Room } from '@/api/types'

export function useRoomLocationOptions() {
  const buildings = ref<Building[]>([])
  const floors = ref<Floor[]>([])
  const rooms = ref<Room[]>([])

  async function loadBuildings() {
    buildings.value = (await pageBuildings({ page: 1, size: 1000 })).records
  }

  // 请求序号，用于竞态防护：请求返回时若已发起更新的请求，说明选中值已切换，丢弃过期结果
  let floorsRequestId = 0
  let roomsRequestId = 0

  async function loadFloors(buildingId?: number) {
    const requestId = ++floorsRequestId
    const result = buildingId ? await listFloors(buildingId) : []
    if (requestId !== floorsRequestId) return
    floors.value = result
    rooms.value = []
  }

  async function loadRooms(buildingId?: number, floorId?: number) {
    const requestId = ++roomsRequestId
    const result = buildingId && floorId
      ? (await pageRooms({ buildingId, floorId, page: 1, size: 1000 })).records
      : []
    if (requestId !== roomsRequestId) return
    rooms.value = result
  }

  return { buildings, floors, rooms, loadBuildings, loadFloors, loadRooms }
}