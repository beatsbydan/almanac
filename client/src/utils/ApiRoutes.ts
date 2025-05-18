const baseRoute = "/api/v1"
const authBaseRoute = "/auth"
const classRoomBaseRoute = "/classrooms"
const timeTableBaseRoute = "/timetables"
const courseBaseRoute = "/courses"
const departmentBaseRoute = "/departments"
const collegeBaseRoute = "/colleges"
const scheduleBaseRoute = "/schedules"

const authRoutes = {
    register: `${baseRoute}${authBaseRoute}/register`,
    login: `${baseRoute}${authBaseRoute}/login`,
    verify_email: `${baseRoute}${authBaseRoute}/verify-email`,
    verify_otp: `${baseRoute}${authBaseRoute}/verify-otp`,
    reset_password: `${baseRoute}${authBaseRoute}/reset-password`,
    refresh: `${baseRoute}${authBaseRoute}/refresh`,
    logout: `${baseRoute}${authBaseRoute}/logout`
}

const classRoomRoutes = {
    create: `${baseRoute}${classRoomBaseRoute}/create`,
    update: `${baseRoute}${classRoomBaseRoute}/update/`,
    get: `${baseRoute}${classRoomBaseRoute}`,
    filter: `${baseRoute}${classRoomBaseRoute}/filter`
}

const timeTableRoutes = {
    create: `${baseRoute}${timeTableBaseRoute}/create`,
    update: `${baseRoute}${timeTableBaseRoute}/update/`,
    change_status: `${baseRoute}${timeTableBaseRoute}/`,
    get_active: `${baseRoute}${timeTableBaseRoute}/active`,
    download: `${baseRoute}${timeTableBaseRoute}/download/`,
    deactivate: `${baseRoute}${timeTableBaseRoute}/deactivate`
}

const courseRoutes = {
    create: `${baseRoute}${courseBaseRoute}/create`,
    update: `${baseRoute}${courseBaseRoute}/update/`,
    get: `${baseRoute}${courseBaseRoute}`,
    filter: `${baseRoute}${courseBaseRoute}/filter`
}

const collegeRoutes = {
    create: `${baseRoute}${collegeBaseRoute}/create`,
    update: `${baseRoute}${collegeBaseRoute}/update/`,
    get: `${baseRoute}${collegeBaseRoute}`,
}

const departmentRoutes = {
    create: `${baseRoute}${departmentBaseRoute}/create`,
    update: `${baseRoute}${departmentBaseRoute}/update/`,
    get: `${baseRoute}${departmentBaseRoute}`,
    filter: `${baseRoute}${departmentBaseRoute}/filter`,
}

const scheduleRoutes = {
    create: `${baseRoute}${scheduleBaseRoute}/create`,
    update: `${baseRoute}${scheduleBaseRoute}/update/`,
    get: `${baseRoute}${scheduleBaseRoute}`,
    filter: `${baseRoute}${scheduleBaseRoute}/filter/`,
    delete: `${baseRoute}${scheduleBaseRoute}/`,
    suggestions: `${baseRoute}${scheduleBaseRoute}/suggestions`
}

const ApiRoutes = {
    AUTH: authRoutes,
    CLASS_ROOM: classRoomRoutes,
    TIMETABLE: timeTableRoutes,
    COURSE: courseRoutes,
    COLLEGE: collegeRoutes,
    DEPARTMENT: departmentRoutes,
    SCHEDULE: scheduleRoutes
}

export default ApiRoutes;