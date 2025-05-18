import { GenericObjectType } from "../../../utils/Types";

const isEmpty = (value: any) => {
    return value?.length === 0 || !value || value === "";
}

export const ValidateClassroomForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const {college, ...rest} = data;
    const {className, classSize} = rest;

    if(isEmpty(className)){
        error.message = "Class name cannot be empty."
        errors.className = {...error}
    }
    if(isEmpty(classSize)){
        error.message = "Class size cannot be empty."
        errors.classSize = {...error}
    }

    const isValid = Object.values(rest).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateCollegeForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { collegeShortName, collegeName} = data;

    if(isEmpty(collegeName)){
        error.message = "College name cannot be empty."
        errors.collegeName = {...error}
    }
    if(isEmpty(collegeShortName)){
        error.message = "College short name cannot be empty."
        errors.collegeShortName = {...error}
    }

    const isValid = Object.values(data).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateDepartmentForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { departmentName, departmentShortName, college } = data;

    if(isEmpty(college)){
        error.message = "College name cannot be empty."
        errors.college = {...error}
    }
    if(isEmpty(departmentShortName)){
        error.message = "Department short name cannot be empty."
        errors.departmentShortName = {...error}
    }
    if(isEmpty(departmentName)){
        error.message = "Department name cannot be empty."
        errors.departmentName = {...error}
    }

    const isValid = Object.values(data).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateCourseForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { 
        isBatched, isGeneral, isShared, ...rest
    } = data;

    const {
        courseTitle,
        courseUnit,
        courseCodes,
        courseParticipants,
        expectedClassSize
    } = rest
    
    if(isEmpty(courseTitle)){
        error.message = "Course title cannot be empty."
        errors.courseTitle = {...error}
    }
    if(isEmpty(courseUnit)){
        error.message = "Course unit cannot be empty."
        errors.courseUnit = {...error}
    }
    if(isEmpty(courseCodes)){
        error.message = "Course code(s) cannot be empty."
        errors.courseCodes = {...error}
    }
    if(isEmpty(courseParticipants)){
        error.message = "Course Participants cannot be empty."
        errors.levels = {...error}
    }
    if(isEmpty(expectedClassSize)){
        error.message = "Class size cannot be empty."
        errors.expectedClassSize = {...error}
    }
    const isValid = Object.values(rest).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateCourseParticipantForm = (data: GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { level, departments } = data;

    if(isEmpty(level)){
        error.message = "Level cannot be empty."
        errors.level= {...error}
    }
    if(isEmpty(departments)){
        error.message = "Departments cannot be empty."
        errors.departments= {...error}
    }

    const isValid = Object.values(data).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateTimeTableForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { active, canCreateSchedules, ...rest } = data;

    const {
        semester, creationStartDate, creationEndDate
    } = rest
    
    if(isEmpty(semester)){
        error.message = "Semester cannot be empty."
        errors.semester = {...error}
    }
    if(isEmpty(creationStartDate)){
        error.message = "Start Date cannot be empty."
        errors.creationStartDate = {...error}
    }
    if(isEmpty(creationEndDate)){
        error.message = "End Date cannot be empty cannot be empty."
        errors.creationEndDate = {...error}
    }
    
    const isValid = Object.values(rest).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}

export const ValidateScheduleForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { userId, recurringDay, startTime, timeDuration, courseCode, classroomCodes } = data;

    if(isEmpty(userId)){
        error.message = "User cannot be empty."
        errors.recurringDay = {...error}
    }
    if(isEmpty(recurringDay)){
        error.message = "Day cannot be empty."
        errors.recurringDay = {...error}
    }
    if(isEmpty(startTime)){
        error.message = "Time cannot be empty."
        errors.startTime = {...error}
    }
    if(isEmpty(timeDuration)){
        error.message = "Duration cannot be empty cannot be empty."
        errors.timeDuration = {...error}
    }
    if(isEmpty(courseCode)){
        error.message = "Course code cannot be empty cannot be empty."
        errors.course = {...error}
    }
    if(isEmpty(classroomCodes)){
        error.message = "Classroom(s) cannot be empty cannot be empty."
        errors.classrooms = {...error}
    }
    
    const isValid = Object.values(data).every(input => !isEmpty(input))

    errors.none = isValid

    return errors;
}