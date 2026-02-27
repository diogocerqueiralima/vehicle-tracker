import {createDataTableContent} from "@/components/datatable/DataTableContent";
import {createDataTableContext} from "@/context/DataTableContext";
import React, {useEffect, useState} from "react";
import {createDataTableHeader} from "@/components/datatable/DataTableHeader";
import {createDataTableFooter} from "@/components/datatable/DataTableFooter";
import {Page} from "@/domain/Page";

export interface RootProps<T> {

    children: React.ReactNode
    getPage: (number: number, filter: string) => Promise<Page<T>>

}

export function createDataTable<T extends object>() {

    const { Provider, useDataTable } = createDataTableContext<T>()
    const Content = createDataTableContent<T>({ useDataTable })
    const Header = createDataTableHeader<T>({ useDataTable })
    const Footer = createDataTableFooter<T>({ useDataTable })

    function Root({ children, getPage }: RootProps<T>) {

        const [items, setItems] = useState<T[]>([])
        const [totalItems, setTotalItems] = useState(0)
        const [currentPage, setCurrentPage] = useState(1)
        const [totalPages, setTotalPages] = useState(1)
        const [filter, setFilter] = useState("")

        useEffect(() => {

            console.log("Fetching page", currentPage)

            getPage(currentPage, filter)
                .then(page => {
                    setItems(page.items)
                    setTotalItems(page.totalItems)
                    setTotalPages(page.totalPages)
                })
                .catch(error => console.error(error))

        }, [getPage, currentPage, filter])

        return (
            <Provider value={
                {
                    items, totalItems, totalPages, currentPage, filter,
                    firstPage: () => setCurrentPage(1),
                    lastPage: () => setCurrentPage(totalPages),
                    nextPage: () => setCurrentPage(prev => Math.min(prev + 1, totalPages)),
                    previousPage: () => setCurrentPage(prev => Math.max(prev - 1, 1)),
                    canAdvance: () => currentPage < totalPages,
                    canBack: () => currentPage > 1,
                    isFirstPage: () => currentPage === 1,
                    isLastPage: () => currentPage === totalPages,
                    updateFilter: (value: string) => setFilter(value)
                }
            }>
                <div className={`flex flex-col bg-surface rounded-sm shadow-md text-sm overflow-x-auto`}>
                    { children }
                </div>
            </Provider>
        )
    }

    return {
        Root,
        Header,
        Content,
        Footer
    }

}