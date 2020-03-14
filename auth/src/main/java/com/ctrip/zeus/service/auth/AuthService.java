package com.ctrip.zeus.service.auth;


import com.ctrip.zeus.auth.impl.AuthorizeException;

/**
* Created by fanqq on 2016/7/20.
*/
public interface AuthService {
    /**
     *
     * @param userName login user name
     * @param ops ops type
     * @param dataType data type
     * @param id data id. if id == 0,means all the ids.
     * @throws AuthorizeException
     */

    void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, String id) throws AuthorizeException;
     /**
     *
     * @param userName login user name
     * @param ops ops type
     * @param dataType data type
     * @param id data id. if id == 0,means all the ids.
     * @throws AuthorizeException
     */
    void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, String[] id) throws AuthorizeException;

     /**
     *
     * @param userName login user name
     * @param ops ops type
     * @param dataType data type
     * @param id data id. if id == 0,means all the ids.
     * @throws AuthorizeException
     */

    void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, Long id) throws AuthorizeException;
     /**
     *
     * @param userName login user name
     * @param ops ops type
     * @param dataType data type
     * @param id data id. if id == 0,means all the ids.
     * @throws AuthorizeException
     */
    void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, Long[] id) throws AuthorizeException;


    /**
     *
     * @param userName login user name
     * @param ops ops type
     * @param dataType data type
     * @param id data id. if id == 0,means all the ids.
     * @throws AuthorizeException
     */

    void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, String id) throws AuthorizeException;
    void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, String[] id) throws AuthorizeException;
    void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, Long[] id) throws AuthorizeException;
    void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, Long id) throws AuthorizeException;

}
