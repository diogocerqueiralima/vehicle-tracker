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

        const { currentPage, totalPages, firstPage, lastPage, nextPage, previousPage, isFirstPage, isLastPage, canBack, canAdvance } = useDataTable()

        return (

            <div className={`flex flex-row items-center justify-between px-8 py-4`}>

                <span className={`text-sm opacity-40`}>Página {currentPage} de {totalPages}</span>

                <div className={`flex flex-row items-center gap-2`}>

                    <MdKeyboardDoubleArrowLeft
                        className={`${isFirstPage() ? "bg-surface-muted cursor-default" : "bg-background duration-200 hover:bg-highlight cursor-pointer" } rounded-md px-1`}
                        size={28}
                        onClick={() => firstPage()}
                    />
                    <MdKeyboardArrowLeft
                        className={`${canBack() ? "bg-background duration-200 hover:bg-highlight cursor-pointer" : "bg-surface-muted cursor-default"} rounded-md px-1`}
                        size={28}
                        onClick={() => previousPage()}
                    />
                    <MdKeyboardArrowRight
                        className={`${canAdvance() ? "bg-background duration-200 hover:bg-highlight cursor-pointer" : "bg-surface-muted cursor-default"} rounded-md px-1`}
                        size={28}
                        onClick={() => nextPage()}
                    />
                    <MdKeyboardDoubleArrowRight
                        className={`${isLastPage() ? "bg-surface-muted cursor-default" : "bg-background duration-200 hover:bg-highlight cursor-pointer" } rounded-md px-1`}
                        size={28}
                        onClick={() => lastPage()}
                    />

                </div>

            </div>

        )

    }

}