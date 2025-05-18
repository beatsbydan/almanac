import { useContext } from "react";
import { AuthContext } from "../contexts/AuthContext";
import { ApiContext } from "../contexts/ApiContext";
import { NavContext } from "../contexts/NavContext";

export const useAuth = () => useContext(AuthContext);
export const useApi = () => useContext(ApiContext);
export const useNav = () => useContext(NavContext);