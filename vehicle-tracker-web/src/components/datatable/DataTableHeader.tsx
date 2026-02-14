import {DataTableContextProps} from "@/context/DataTableContext";
import {CreateContentProps} from "@/components/datatable/DataTableContent";
import InputBar from "@/components/InputBar";
import {IoIosSearch} from "react-icons/io";
import {useState} from "react";

export interface CreateHeaderProps<T extends object> {
    useDataTable: () => DataTableContextProps<T>
}

export interface HeaderProps {

    title: string

}

export function createDataTableHeader<T extends object>({ useDataTable }: CreateContentProps<T>) {

    return function Header({ title }: HeaderProps) {

        const { totalItems } = useDataTable()
        const [search, setSearch] = useState("")

        return (

            <div className={`flex flex-row items-center justify-between p-8`}>

                <div className={`flex flex-row gap-4`}>
                    <h1 className={`font-bold text-xl`}> {title} </h1>
                    <span className={`text-sm px-2 py-1 rounded-xl bg-highlight`}>{ totalItems } items</span>
                </div>
                
                <InputBar
                    icon={<IoIosSearch size={24} />}
                    placeholder={"Procure por número de série..."}
                    value={search}
                    onChange={value => setSearch(value)}
                />


            </div>

        )

    }

}