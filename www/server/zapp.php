<!doctype html>
<html>
<head>
<?php include '../_head.php' ?>
<title>ZAPP - Server - Zerograph</title>
</head>
<body>

<?php include '../_header.php' ?>

<?php include '_menu.php' ?>

<main>

<h1>Zerograph Application/Presentation Protocol (ZAPP) V1</h1>

<h2>1. Introduction</h2>
<p>The Zerograph Application/Presentation Protocol (ZAPP) provides an OSI levels
6 and OSI level 7 text-based request-response protocol that is used to define
communications between client applications that require a graph database and
server applications that provide a Neo4j data store.
</p>
<p>The reference implementation for ZAPP - the Zerograph server bundle - transmits
all messages over ZeroMQ. This means of transmission is independent to the
protocol however and any request-response mechanism that supports textual
messaging should be able to support ZAPP.
</p>

<h2>2. General</h2>

<h3>2.1. Encoding</h3>
<p>All requests and responses MUST only use characters from the basic ASCII set,
i.e. 0x00 to 0x7F. Extended characters can be represented with the appropriate
JSON or YAML encoding: generally the "\uXXXX" sequence.
</p>

<h3>2.2. End of Line Sequences</h3>
<p>Lines may be separated by any common end-of-line sequence as defined below:
</p>
<pre>eol := &lt;CR&gt; | &lt;LF&gt; | &lt;CR&gt;&lt;LF&gt;
</pre>

<h2>3. Presentation Protocol (OSI Level 6)</h2>

<h3>3.1. General Request/Response Layout</h3>
<p>At the highest level, ZAPP clients package a number of individual requests into
a request batch, one per line. These include a JSON payload which MUST not
contain any end of line sequences itself so as to avoid ambiguity. Responses
are encoded using YAML and contain one document per request line. A final,
optional response document MAY be appended if required. This could contain
summary or error information for the entire batch.
</p>
<p>This document specifies no explicit correspondence between a request batch and
any underlying database transactions or other state. The framing of messages
is likewise unspecified and request and response batches may be transmitted in
multiple parts or in a single part as necessary. An implementation MAY also
allow responses to start transmission before the corresponding request has
been fully received. This capability will of course be dependent on support
from the underlying transmission protocol.
</p>

<h3>3.2. Requests</h3>
<p>Each individual request within a batch occupies a single line and comprises
two mandatory elements and one optional element, each separated by single space
characters. Blank lines and a trailing end-of-line sequence MAY be included but
should be ignored by the receiving server.
</p>
<p>The precise layout of a request batch (ignoring blank lines) is described
below:
</p>

<pre>request_batch := request [eol request]* [eol]
request       := method &lt;SP&gt; resource [&lt;SP&gt; arguments]
method        := &lt;ALPHA&gt;+
resource      := (&lt;ALPHA&gt; | "_") (&lt;ALPHA&gt; | &lt;DIGIT&gt; | "_")*
arguments     := &lt;JSON_OBJECT&gt;
</pre>

<p>Specific method and resource terms are defined by the application layer but
- by convention - methods SHOULD be encoded in upper case
and resources in camel case. End of line characters within the JSON arguments
are expressly forbidden to avoid ambiguities with individual request
delimitation.
</p>

<h3>3.3. Responses</h3>
<p>A response batch consists of a sequence of YAML documents, one for each request
sent in the corresponding request batch. A final document may also be included
which could, for example, contain a statistical summary of the batch's
execution or any relevant warnings or errors. If omitted, an empty summary
document may be inferred.
</p>
<p>Each YAML response document - separated by the "---" sequence - should consist
of a single block style mapping. Within this mapping, four possible keys are
permitted: "head", "body", foot" and "error". Although these are all optional,
they MUST appear in this order whenever used. That is: if the "head" key is
provided, it MUST precede the "body" key and the "foot" key MUST follow the
"body" key in a similar way. If included, the "error" key SHOULD terminate the
response. This means that a response containing only an "error" key is valid.
</p>
<p>Other documents may follow an "error" key but any information following an
"error" within the same document SHOULD be ignored by the client application.
This should also apply to any keys returned in a response other than the ones
specified here.
</p>
<p>The values attached to each key are left for definition by the application
layer protocol and may be scalar values or YAML collections in either
flow or block style. When block style is used, an indentation level of two
spaces is preferred.
</p>
<p>The precise layout of a response batch is described below:
</p>
<pre>response_batch   := response ["---" response]*
response         := [head] [body] [foot] [error]
head             := "head:" [&lt;SP&gt;] &lt;YAML_MAPPING&gt; eol
body             := "body:" [&lt;SP&gt;] &lt;YAML_VALUE&gt; eol
foot             := "foot:" [&lt;SP&gt;] &lt;YAML_MAPPING&gt; eol
error            := "error:" [&lt;SP&gt;] &lt;YAML_VALUE&gt; eol
</pre>

<h2>4. Application Protocol (OSI Level 7)</h2>

<p><em>TODO</em>
</p>

<h2>5. Examples</h2>
<p>The example below shows an example request batch (in bold) and its
corresponding responses (in normal type):
</p>

<pre><strong>CREATE Thing {"label": "foo", "size": 13}
GET Things {"label": "foo"}</strong>
body: !Thing {"id": 42, "label": "foo", "size": 13}
foot: {"stats": {"things_created": 1}}
---
body:
  - !Thing {"id": 41, "label": "foo", "size": 1006}
  - !Thing {"id": 42, "label": "foo", "size": 13}
  - !Thing {"id": 43, "label": "foo", "size": -4}
foot: {"stats": {"things_found": 3}}
</pre>

<h2>6. References</h2>
<ul>
<li>JSON specification, 1st edition<br>
  &lt;<a href="http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf">http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf</a>&gt;
</li>
<li>YAML specification, version 1.2<br>
  &lt;<a href="http://www.yaml.org/spec/1.2/spec.html">http://www.yaml.org/spec/1.2/spec.html</a>&gt;
</li>
</ul>

</main>

<?php include '_footer.php' ?>
</body>
</html>
