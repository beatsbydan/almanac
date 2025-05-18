import { ReactNode } from "react"

export interface ErrorType{
    valid: boolean,
    message: string
}

export interface GenericObjectType{
    [key: string]: any
}

export interface ModalType{
    isOpen: boolean,
    closeModal: () => void,
    children: ReactNode,
}

export interface SuggestionModalType{
    closeModal: () => void,
    saveSuggestion: (e: GenericObjectType) => void
}

export interface ModalFormType{
    inputValues?: GenericObjectType
}

export interface DropDownType{
    save?: (value: string) => void,
    saveOptions?: (values: GenericObjectType[]) => void,
    placeHolder?: string,
    label?: string,
    options?: any[],
    optionsApiRoute?: string,
    value?: string | undefined,
    isListInput?: boolean,
    canSearch?: boolean,
    searchPlaceholder?: string,
    searchOnly?: boolean,
    error?: ErrorType
}

export interface FilterType{
    setFilter: (e: string) => void
}

export interface FilterParamsType{
    setFilter: (e: GenericObjectType) => void
}

export interface FilterContextType{
    collegeFilter: string,
    changeCollegeFilter: (e: string) => void 
}

export interface RenderedDropdownDisplayType{
    options: any[] | undefined,
    isLoading: boolean,
    handleChange: (e: string) => void
}

export interface InputType{
    type: string,
    delay?: number,
    placeholder?: string,
    label?: string,
    value?: string,
    name?: string,
    error?: ErrorType,
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void,
}

export interface RadioInputType{
    label: string,
    value: boolean,
    handleChange: () => void,
    delay: number
}

export interface ListInputType{
    save: (values: string[]) => void,
    saveOptions?: (values: GenericObjectType[]) => void,
    inputValuesList?: string[],
    placeHolder?: string,
    searchPlaceHolder?: string,
    emptyText: string,
    isDropdown: boolean,
    label?: string,
    isSearch?: boolean,
    delay?: number,
    optionsApiRoute?: string,
    canSearch?: boolean,
    options?: any[],
    error: ErrorType
}

export interface RegisterFormType{
    inputValues: GenericObjectType,
    inputErrors: GenericObjectType,
    handleLevelChange?:(value: string) => void,
    handleDepartmentChange?:(value: string) => void,
    handleTitleChange?:(value: string) => void,
    handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void,
}

export interface CourseParticipantFormType{
    participant: GenericObjectType | null,
    closeModal: () => void, 
    save: (e: GenericObjectType) => void,
    update: (e: GenericObjectType) => void
}

export interface courseParticipantsType{
    save: (e: GenericObjectType)=> void, 
    removeParticipant:(e: GenericObjectType)=> void, 
    updateParticipant:(e: GenericObjectType)=> void, 
    participants: GenericObjectType[]
}

export interface DateType{
    title:string,
    placeholder:string,
    value:string,
    handleChange:(date: string) => void,
    delay: number,
    error: ErrorType
}

export interface ToggleType{
    styleClass: string,
    action: ()=>void,
    color: string,
    condition: string | null
}

export interface ButtonType{
    processing: boolean,
    children: ReactNode
    styleClass: string,
    spinnerStyleClass: string,
    delay: number,
    onClick: (() => void) | ((e: any) => void)
}

export interface AuthContextType{
    user: GenericObjectType | null,
    token: string | null,
    role: string | null,
    isLoggedIn: string | boolean,
    login: (payload: GenericObjectType) => void,
    updateToken: (token: string) => void,
    logout: () => void,
    getToken: () => string | null,
}

export interface AuthCardType{
    children: ReactNode,
    header: string,
    subHeader: string
}

// ANIMATIONS
export interface ComponentRevealType{
    type: string,
    styleClass: string,
    delay: number,
    children?: ReactNode,
    src?: string | undefined
    alt?: string | undefined
}

export interface InputComponentRevealType{
    placeholder?: string,
    styleClass?: string,
    type?: string,
    delay?: number,
    value?: string,
    name?: string,
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void
}

// API

export interface ApiResponseType{
    success: boolean,
    responseBody: GenericObjectType | null,
    error: GenericObjectType | null
}

export interface ApiContextType{
    makePostRequest: (
        url: string, 
        payload: GenericObjectType,
        params?: GenericObjectType | undefined
    ) => Promise<ApiResponseType>,
    makePutRequest: (
        url: string, 
        payload?: GenericObjectType| undefined,
        params?: GenericObjectType | undefined
    ) => Promise<ApiResponseType>,
    makeGetRequest: (
        url: string, 
        params?: GenericObjectType | undefined
    ) => Promise<ApiResponseType>,
    makeDownloadRequest: (
        url: string, 
        params?: GenericObjectType | undefined
    ) => Promise<any>,
    makeDeleteRequest: (
        url: string,
    ) => Promise<ApiResponseType>
}

// NAVIGATION
export interface NavLinkType{
    children: ReactNode,
    path: string,
    name: string,
    delay: number,
    initialTopMargin: number
}

export interface NavContextType{
    currentPage: string,
    changeCurrentPage: (page: string) => void
}

export interface ListEntityType{
    selectEntity: (e: GenericObjectType) => void,
    deleteEntity?: (code: string | undefined) => void
}
export interface EntityListType{
    list: GenericObjectType[],
    emptyText: string,
    loading: boolean,
    selectEntity: (e: GenericObjectType) => void,
    deleteEntity?: (code: string | undefined) => void
}

export interface ActiveTimeTableType{
    timetable: GenericObjectType | undefined,
    openModal: ()=> void,
    refetchMethod: ()=> void
}

export interface TableType{
    headers: string[],
    list: GenericObjectType[],
    emptyText: string,
    loading: boolean,
    canEdit?: boolean,
    canDelete?: boolean,
    isFilterable?: boolean,
    listStyle: string,
    tableHeight?: string,
    delay?: number, 
    displayKeys: string[],
    deleteApiRoute?: string,
    selectEntity?: (e: GenericObjectType) => void,
    refetchMethod?: () => void
}

export interface TableRowType{
    index: number,
    listStyle: string,
    entity: GenericObjectType,
    displayKeys: string[],
    canEdit?: boolean,
    canDelete?: boolean,
    deleteApiRoute?: string,
    selectEntity?: (e: GenericObjectType) => void,
    refetchMethod?: () => void
}

export interface DashboardRouteType{
    path: string,
    description: string,
    children: ReactNode
}

export interface PaginationType{
    details: PaginationInfoType,
    method: (value: number) => void
}

export interface PaginationInfoType{
    page: number,
    size: number,
    currentPage: number,
    totalPages: number
}

export interface AppContextType{

}