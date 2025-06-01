import { GenericObjectType } from "../../../utils/Types";

const isEmpty = (string: string) => {
    return string?.length === 0 || !string;
}

const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;

export const ValidateRegistrationForm = (data: GenericObjectType, isStudent: boolean) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors: GenericObjectType = {}
    const { level, firstName, lastName, phone, title, department, ...rest } = data;
    const { userEmail, userId, password, confirmPassword } = rest

    if(isEmpty(userEmail) || !emailRegex.test(userEmail)){
        error.message = "Enter a valid email."
        errors.userEmail = {...error}
    }
    if(isEmpty(userId)){
        error.message = "User ID cannot be empty."
        errors.userId = {...error}
    }
    if(isStudent && isEmpty(level)){
        error.message = "Level cannot be empty."
        errors.level = {...error}
    }
    if(!isStudent && isEmpty(firstName)){
        error.message = "First Name cannot be empty."
        errors.firstName = {...error}
    }
    if(!isStudent && isEmpty(lastName)){
        error.message = "Last Name cannot be empty."
        errors.lastName = {...error}
    }
    if(!isStudent && isEmpty(phone)){
        error.message = "Phone cannot be empty."
        errors.phone = {...error}
    }
    if(!isStudent && isEmpty(title)){
        error.message = "Title cannot be empty."
        errors.title = {...error}
    }
    if(isEmpty(department)){
        error.message = "Department cannot be empty."
        errors.department = {...error}
    }
    if(password?.length < 8){
        error.message = "Password should be at least 8 characters."
        errors.password = {...error}
    }
    if(confirmPassword?.length < 8){
        error.message = "Password should be at least 8 characters."
        errors.confirmPassword = {...error}
    }
    if(password !== confirmPassword){
        error.message = "Passwords don't match."
        errors.password = {...error}
        errors.confirmPassword = {...error}
    }

    const studentFields = {...rest, level, department}
    const staffFields = {...rest, firstName, lastName, phone, title}

    const isValid = Object.values(isStudent? studentFields : staffFields).every(input => !isEmpty(input)) && emailRegex.test(userEmail) && password?.length >= 8 && password === confirmPassword

    errors.none = isValid

    return errors;
}

export const ValidateLoginForm = (data: GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors:GenericObjectType = {}
    const {userId, password} = data;

    if(isEmpty(userId)){
        error.message = "User ID cannot be empty."
        errors.userId = {...error}
    }
    if(isEmpty(password)){
        error.message = "Password cannot be empty."
        errors.password = {...error}
    }

    const isValid = Object.values(data).every(input => !isEmpty(input))

    errors.none = isValid;

    return errors;
}

export const ValidateEmailVerificationForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors:GenericObjectType = {}
    const {userEmail} = data;

    if(!emailRegex.test(userEmail)){
        error.message = "Enter a valid email."
        errors.userEmail = {...error}
    }
    else{
        errors.none = true;
    }

    return errors;
}

export const ValidateResetPasswordForm = (data:GenericObjectType) => {
    let error:GenericObjectType = {
        valid: true,
        message: "" 
    }
    
    let errors:GenericObjectType = {}
    const {password, confirmPassword} = data;

    if(password?.length < 8){
        error.message = "Password too short."
        errors.password = {...error}
    }
    if(confirmPassword?.length < 8){
        error.message = "Password too short."
        errors.confirmPassword = {...error}
    }
    if(password !== confirmPassword){
        error.message = "Password too short."
        errors.password = {...error}
        errors.confirmPassword = {...error}
    }

    if(password.length >= 8 && (password === confirmPassword)){
        errors.none = true;
    }

    return errors;
}