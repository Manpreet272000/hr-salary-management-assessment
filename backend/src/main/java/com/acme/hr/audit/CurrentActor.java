package com.acme.hr.audit;

/**
 * Who is making the current change. There is no login yet, so this is the HR Manager by default;
 * with SSO, an implementation reading the authenticated user replaces {@link RequestHeaderActor}.
 */
public interface CurrentActor {

    String name();
}
