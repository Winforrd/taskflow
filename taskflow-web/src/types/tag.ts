export type TagScopeType =
  | 'TEAM'
  | 'PROJECT'


export interface Tag {

  id: number

  scopeType: TagScopeType

  scopeId: number

  name: string

  color: string

  creatorId: number

  createdTime?: string

}


export interface TagCollection {

  canManage: boolean

  tags: Tag[]

}


export interface TaskTagContext {

  taskId: number

  selectedTags: Tag[]

  availableTags: Tag[]

  canEdit: boolean

  canManageProjectTags: boolean

}


export interface CreateTagRequest {

  name: string

  color?: string

}


export interface UpdateTagRequest {

  name: string

  color?: string

}


export interface UpdateTaskTagsRequest {

  tagIds: number[]

}