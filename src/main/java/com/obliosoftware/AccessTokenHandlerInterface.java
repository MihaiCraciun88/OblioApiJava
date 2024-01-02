package com.obliosoftware;

public interface AccessTokenHandlerInterface {
    /**
     *  @return AccessToken
     */
    public AccessToken get();
    
    /**
     *  @param accessToken
     *  @return void
     */
    public void set(AccessToken accessToken);
}