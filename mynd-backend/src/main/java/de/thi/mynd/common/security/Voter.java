/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.common.security;

import io.quarkus.security.identity.SecurityIdentity;

public interface Voter<T> {

  boolean supports(String attribute, Object subject);

  boolean vote(SecurityIdentity identity, String attribute, T subject);
}
