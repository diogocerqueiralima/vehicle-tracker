import {DataTableContextProps} from "@/context/DataTableContext";
import {CreateContentProps} from "@/components/datatable/DataTableContent";
import {
    MdKeyboardArrowLeft,
    MdKeyboardArrowRight,
    MdKeyboardDoubleArrowLeft,
    MdKeyboardDoubleArrowRight
} from "react-icons/md";

export interface CreateFooterProps<T extends object> {
    useDataTable: () => DataTableContextProps<T>
}

export function createDataTableFooter<T extends object>({ useDataTable }: CreateContentProps<T>) {

    return function Header() {

        const { currentPage, totalPages } = useDataTable()

        return (

            <div className={`flex flex-row items-center justify-between px-8 py-4`}>

                <span className={`text-sm opacity-40`}>Página {currentPage} de {totalPages}</span>

                <div className={`flex flex-row items-center gap-2`}>

                    <MdKeyboardDoubleArrowLeft
                        className={`bg-background rounded-md px-1 cursor-pointer duration-200 hover:bg-highlight`}
                        size={28}
                    />
                    <MdKeyboardArrowLeft
                        className={`bg-background rounded-md px-1 cursor-pointer duration-200 hover:bg-highlight`}
                        size={28}
                    />
                    <MdKeyboardArrowRight
                        className={`bg-background rounded-md px-1 cursor-pointer duration-200 hover:bg-highlight`}
                        size={28}
                    />
                    <MdKeyboardDoubleArrowRight
                        className={`bg-background rounded-md px-1 cursor-pointer duration-200 hover:bg-highlight`}
                        size={28}
                    />

                </div>

            </div>

        )

    }

}