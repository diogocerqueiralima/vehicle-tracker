export interface Page<T> {

    page: number
    totalPages: number
    totalItems: number
    items: T[]

}