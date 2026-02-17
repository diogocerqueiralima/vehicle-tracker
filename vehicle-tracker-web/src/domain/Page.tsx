export interface Page<T> {

    page: number
    totalItems: number
    totalPages: number
    content: T[]

}