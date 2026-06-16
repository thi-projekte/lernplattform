/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 *  Copyright (c) 2026 Projektgruppe MYnd B.Sc. WINF THI <mab8881@thi.de>
 *  
 *  For the full copyright and license information, please view the LICENSE
 *  file that was distributed with this source code.
 */

package de.thi.mynd.topic.dto.content;

import java.net.URL;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class AudioFileElementDto extends ContentElementDto {

  public URL presignedUrl;

  public String originalFileName;
}
