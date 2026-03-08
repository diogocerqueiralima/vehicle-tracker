export interface PageDTO<T> {

    page: number
    total_pages: number
    total_items: number
    items: T[]

}