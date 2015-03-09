package org.basex.http.restxq;

import org.basex.io.*;
import org.junit.*;

/**
 * This test contains RESTXQ filters.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RestXqInputTest extends RestXqTest {
  /**
   * JSON: {@code %input} annotation, content-type parameters.
   * @throws Exception exception
   */
  @Test
  public void json() throws Exception {
    // test input annotation
    post("declare %R:POST('{$x}') %R:path('') %input:json('lax=no') function m:f($x) {$x/*/*};",
        "", "{ \"A_B\": \"\" }", "application/json", "<A__B/>");
    post("declare %R:POST('{$x}') %R:path('') %input:json('lax=true') function m:f($x) {$x/*/*};",
        "", "{ \"A_B\": \"\" }", MimeTypes.APP_JSON, "<A_B/>");

    // test content-type parameters
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};",
        "", "{ \"A_B\": \"\" }", MimeTypes.APP_JSON + ";lax=false", "<A__B/>");
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};",
        "", "{ \"A_B\": \"\" }", MimeTypes.APP_JSON + ";lax=yes", "<A_B/>");

    // test default
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};",
        "", "{ \"A_B\": \"\" }", MimeTypes.APP_JSON, "<A__B/>");
  }

  /**
   * CSV: {@code %input} annotation, content-type parameters.
   * @throws Exception exception
   */
  @Test
  public void csv() throws Exception {
    // test input annotation
    post("declare %R:POST('{$x}') %R:path('') %input:csv('header=no') function m:f($x) {$x//A};",
        "", "A\n1", MimeTypes.TEXT_CSV, "");
    post("declare %R:POST('{$x}') %R:path('') %input:csv('header=yes') function m:f($x) {$x//A};",
        "", "A\n1", MimeTypes.TEXT_CSV, "<A>1</A>");

    // test content-type parameters
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};",
        "", "A\n1", MimeTypes.TEXT_CSV + ";header=no", "");
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};",
        "", "A\n1", MimeTypes.TEXT_CSV + ";header=yes", "<A>1</A>");

    // test default
    post("declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};",
        "", "A\n1", MimeTypes.TEXT_CSV, "");
  }
}