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

  async function loadFloors(buildingId?: number) {
    floors.value = buildingId ? await listFloors(buildingId) : []
    rooms.value = []
  }

  async function loadRooms(buildingId?: number, floorId?: number) {
    rooms.value = buildingId && floorId
      ? (await pageRooms({ buildingId, floorId, page: 1, size: 1000 })).records
      : []
  }

  return { buildings, floors, rooms, loadBuildings, loadFloors, loadRooms }
}