package org.hspconsortium.platform.messaging.controller.user;

import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;

public interface UserController {
    String health(String s);

    int createUser(SandboxUserInfo user);

    void updateProfile();
}
