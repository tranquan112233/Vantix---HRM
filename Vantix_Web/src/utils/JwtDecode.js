import { jwtDecode } from "jwt-decode";

export function getUserIdFromToken(){

    const token = localStorage.getItem("token");

    if(!token) return null;

    const decoded = jwtDecode(token);

    return decoded.sub;
}