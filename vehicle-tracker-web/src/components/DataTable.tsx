export interface DataTableHeaderItem {

    name: string
    label: string

}

export interface DataTableHeader {

    items: DataTableHeaderItem[]

}

export const DataTableHighlightType = {
    POSITIVE: { name: "POSITIVE", color: "positive" },
    NEGATIVE: { name: "NEGATIVE", color: "negative" }
} as const;

export type DataTableHighlightType =
    typeof DataTableHighlightType[keyof typeof DataTableHighlightType];

export interface DataTableCellHighlight {

    name: string;
    type: DataTableHighlightType;

}

export interface DataTableItem<T extends Record<string, unknown>> {

    item: T
    highlights: DataTableCellHighlight[]

}

export interface DataTableProps<T extends Record<string, unknown>> {

    header: DataTableHeader
    content: DataTableItem<T>[]

}

export default function DataTable<T extends Record<string, unknown>>({ header, content }: DataTableProps<T>) {
    return (

        <div className={`flex flex-col bg-surface rounded-sm shadow-md text-sm`}>

            <div className={`p-8`}>

                <h1 className={`text-xl font-bold`}>Certificados</h1>

            </div>

            <table>

                <thead>

                    <tr className={`opacity-40 border-y border-foreground-muted cursor-default`}>

                        {

                            header.items.map((item, index) => (

                                <th key={index} className={`text-start px-8 py-4`}>
                                    {item.label}
                                </th>

                            ))

                        }

                    </tr>

                </thead>

                <tbody>

                    {

                        content.map((row, rowIndex) => (

                            <tr
                                key={rowIndex}
                                className={`
                                    border-b border-b-foreground-muted last:border-b-0
                                    hover:bg-surface-muted duration-200 cursor-default
                                `}
                            >

                                {

                                    Object.entries(row.item).map(([key, value], colIndex) => {

                                        const headerItem = header.items
                                            .find(h => h.name === key)
                                        if (!headerItem) return null

                                        const highlight = row.highlights.find(h => h.name === key)
                                        const color = highlight ? highlight.type.color : ""
                                        const highlightClasses = highlight ? `bg-${color}-muted text-${color} py-1 px-2 rounded-full` : ""

                                        return (
                                            <td key={colIndex} className={`px-8 py-4`}>
                                                <span className={`${highlightClasses}`}>{String(value)}</span>
                                            </td>
                                        )

                                    })

                                }

                            </tr>

                        ))

                    }

                </tbody>

            </table>

        </div>

    )
}