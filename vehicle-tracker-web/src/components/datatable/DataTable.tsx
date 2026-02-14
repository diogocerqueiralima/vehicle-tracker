import {createDataTableContent} from "@/components/datatable/DataTableContent";
import {createDataTableContext} from "@/context/DataTableContext";
import React, {useEffect, useState} from "react";
import {createDataTableHeader} from "@/components/datatable/DataTableHeader";
import {createDataTableFooter} from "@/components/datatable/DataTableFooter";

export interface Page<T> {

    items: T[]
    totalItems: number
    totalPages: number
    currentPage: number

}

export interface RootProps<T> {

    children: React.ReactNode
    getPage: (number: number) => Promise<Page<T>>

}

export function createDataTable<T extends object>() {

    const { Provider, useDataTable } = createDataTableContext<T>()
    const Content = createDataTableContent<T>({ useDataTable })
    const Header = createDataTableHeader<T>({ useDataTable })
    const Footer = createDataTableFooter<T>({ useDataTable })

    function Root({ children, getPage }: RootProps<T>) {

        const [items, setItems] = useState<T[]>([])
        const [totalItems, setTotalItems] = useState(0)

        useEffect(() => {

            getPage(1)
                .then(page => {
                    setItems(page.items)
                    setTotalItems(page.totalItems)
                })

        }, [getPage])

        return (
            <Provider value={
                { items, totalItems, totalPages: 1, currentPage: 1 }
            }>
                <div className={`flex flex-col bg-surface rounded-sm shadow-md text-sm`}>
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