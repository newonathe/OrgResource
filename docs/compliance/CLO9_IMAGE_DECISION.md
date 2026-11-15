# CLO9 image requirement decision record

**Status: UNRESOLVED. Image upload and serving are not implemented or claimed as satisfied.**

## What the sources establish

- The CSCI 180.05 syllabus names CLO9 as creating a web application that can receive and serve images over HTTP (syllabus, p. 2). The course outline separately lists image saving to a database under CLO9 (p. 3).
- The project assignment's final-project section describes completing the proposed application and its actual logic, but does not specify an OrgResource image feature or image API (assignment, pp. 2–3).
- The concise OrgResource proposal defines five domain entities and the resource planning/allocation API. Its project description and route catalogue do not define image upload, image retrieval, image ownership, or image persistence.
- The V10 transcript describes the image-server sample as a standalone upload/retrieval example and discusses integrating it with another application as an optional design choice. That lecture demonstrates a course pattern; it does not state that every final project must include images.
- The supplied `files/ImageServerDemo (1).zip` contains a separate `UploadedFile` JPA entity and `/tester/upload` plus `/tester/image?id=...` endpoints. Its `FileManager` writes uploaded filenames to a local `temp/` directory and stores the filename in MySQL. It is a teaching example, not an OrgResource contract. The sample does not provide the validation, upload limits, or filename/path protection needed to adopt it unchanged.

## Why the answer stays unresolved

The syllabus makes CLO9 a course outcome, while the assignment and OrgResource proposal do not say whether image functionality is a required feature of this particular final project. The lecture leaves the integration choice to the student and explicitly describes the example as untied to another application's records. Those sources do not settle whether OrgResource must implement the outcome, how it would relate images to organization records, or whether an additional persistence model is allowed.

The current OrgResource implementation contract preserves exactly five JPA domain entities and the original 25 API operations, with A09 tracked separately. Adding the sample's sixth entity or filesystem behavior without resolving the course requirement would change that contract. Therefore this release does not add image routes, file storage, or image binaries.

## Evidence needed to resolve it

Obtain the current final-project rubric or written instructor direction that answers:

1. Must this final project itself receive and serve images to satisfy CLO9, or is CLO9 assessed independently through the course work?
2. If required, must images be associated with a Department, Project, Resource, or another record?
3. What storage approach and acceptance checks are required, and may that feature add a table/entity or separate service?

Canvas was not inspected during this work. No instructor or Canvas submission was made. Until the requirement is answered, treat CLO9 as a release blocker for any claim of complete course compliance.
