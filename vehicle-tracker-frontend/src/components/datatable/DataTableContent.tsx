import {DataTableContextProps} from "@/context/DataTableContext";
import React from "react";
import Loader from "@/components/Loader";

export interface ContentHeaderItem<T extends object, K extends keyof T = keyof T> {
    name: K
    label: string
    render?: (item: T) => React.ReactNode
}

export interface ContentProps<T extends object> {
    headerItems: readonly ContentHeaderItem<T>[]
}

export interface CreateContentProps<T extends object> {
    useDataTable: () => DataTableContextProps<T>
}

export function createDataTableContent<T extends object>({ useDataTable }: CreateContentProps<T>) {

    function DataTableCell({ children }: { children: React.ReactNode }) {
        return (
            <td className="px-8 py-4">
                {children}
            </td>
        )
    }

    return function Content({ headerItems }: ContentProps<T>) {

        const { items, isLoading, error } = useDataTable()

        return (
            <table>

                <thead>
                <tr className="opacity-40 border-y border-foreground-muted cursor-default">
                    {headerItems.map((headerItem, index) => (
                        <th
                            key={index}
                            className="text-start px-8 py-4"
                        >
                            {headerItem.label}
                        </th>
                    ))}
                </tr>
                </thead>

                <tbody>

                    {
                        isLoading ? (
                            <tr>
                                <td colSpan={headerItems.length} className="py-8 text-center">
                                    <div className="flex justify-center">
                                        <Loader />
                                    </div>
                                </td>
                            </tr>
                        ) : error != null ? (
                            <tr>
                                <td colSpan={headerItems.length} className="py-8 text-center">
                                    <span className={`bg-negative-muted px-8 py-2 rounded-md text-negative`}>{error}</span>
                                </td>
                            </tr>
                        ) : (

                            items.map((item, rowIndex) => (
                                <tr
                                    key={rowIndex}
                                    className="
                                                border-b border-b-foreground-muted
                                                hover:bg-surface-muted duration-200 cursor-default
                                            "
                                >
                                    {headerItems.map((headerItem, colIndex) => {

                                        const value = item[headerItem.name]

                                        return (
                                            <DataTableCell key={colIndex}>
                                                {
                                                    headerItem.render
                                                        ? headerItem.render(item)
                                                        : String(value)
                                                }
                                            </DataTableCell>
                                        )
                                    })}
                                </tr>
                            ))

                        )

                    }

                </tbody>

            </table>
        )
    }
}