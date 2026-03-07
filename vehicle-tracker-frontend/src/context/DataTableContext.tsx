import {createContext, useContext} from "react";

export interface DataTableContextProps<T extends object> {

    items: T[]
    totalItems: number
    totalPages: number
    currentPage: number
    nextPage: () => void
    previousPage: () => void
    lastPage: () => void
    firstPage: () => void
    canAdvance: () => boolean
    canBack: () => boolean
    isFirstPage: () => boolean
    isLastPage: () => boolean
    filter: string
    updateFilter: (value: string) => void,
    isLoading: boolean,
    error: string | null

}

export function createDataTableContext<T extends object>() {

    const DataTableContext = createContext<DataTableContextProps<T> | undefined>(undefined);

    function useDataTable() {

        const context = useContext(DataTableContext);

        if (!context) {
            throw new Error("useDataTable must be used within a DataTableProvider");
        }
        return context;
    }

    return {
        Provider: DataTableContext.Provider,
        useDataTable,
    };

}