export interface DataTableHeaderItem {

    name: string
    label: string

}

export interface DataTableHeader {

    items: DataTableHeaderItem[]

}

export interface DataTableProps<T extends Record<string, unknown>> {

    header: DataTableHeader
    content: T[]

}

export default function DataTable<T extends Record<string, unknown>>({ header, content }: DataTableProps<T>) {
    return (

        <div className={`bg-surface rounded-sm shadow-md text-sm`}>

            <table>

                <thead>

                    <tr className={`opacity-40 border-b border-b-foreground-muted cursor-default`}>

                        {

                            header.items.map((item, index) => (

                                <th key={index} className={`px-8 py-4`}>
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
                                    text-center border-b border-b-foreground-muted last:border-b-0 hover:bg-background
                                    duration-200 cursor-default
                                `}
                            >

                                {

                                    Object.entries(row).map(([key, value], colIndex) => {

                                        const headerItem = header.items
                                            .find(h => h.name === key)
                                        if (!headerItem) return null

                                        return (
                                            <td key={colIndex} className={`px-8 py-4`}>{String(value)}</td>
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