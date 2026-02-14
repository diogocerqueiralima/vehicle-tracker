import {DataTableContextProps} from "@/context/DataTableContext";
import {CreateContentProps} from "@/components/datatable/DataTableContent";

export interface CreateHeaderProps<T extends object> {
    useDataTable: () => DataTableContextProps<T>
}

export interface HeaderProps {

    title: string

}

export function createDataTableHeader<T extends object>({ useDataTable }: CreateContentProps<T>) {

    return function Header({ title }: HeaderProps) {

        const { totalItems } = useDataTable()

        return (

            <div className={`flex flex-row items-center gap-8 p-8`}>

                <h1 className={`font-bold text-xl`}> {title} </h1>
                <span className={`text-sm px-2 py-1 rounded-xl bg-highlight`}>{ totalItems } items</span>

            </div>

        )

    }

}