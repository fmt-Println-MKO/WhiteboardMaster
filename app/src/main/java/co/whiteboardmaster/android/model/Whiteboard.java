package co.whiteboardmaster.android.model;

import java.io.Serializable;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class Whiteboard implements Serializable{

    private long id;
    private String title;
    private String imageFileName;
    private String thumbFileName;
    private String description;
    private long created;
    private long updated;
    private String guid;


    public Whiteboard(long id, String title, String imageFileName, String thumbFileName, String description, long created, long updated, String guid) {
        this.id = id;
        this.title = title;
        this.imageFileName = imageFileName;
        this.thumbFileName = thumbFileName;
        this.description = description;
        this.created = created;
        this.updated = updated;
        this.guid = guid;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getThumbFileName() {
        return thumbFileName;
    }

    public String getDescription() {
        return description;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public String toString() {
        return "Whiteboard{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", imageFileName='" + imageFileName + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", guid='" + guid + '\'' +
                '}';
    }

    public static class WhiteBoardBuilder {

        private long id;
        private String title;
        private String imageFileName;
        private String thumbFileName;
        private String description;
        private long created;
        private long updated;
        private String guid;


        public WhiteBoardBuilder() {
        }
        public WhiteBoardBuilder(Whiteboard wb) {
            this.id = wb.getId();
            this.title = wb.getTitle();
            this.imageFileName = wb.getImageFileName();
            this.thumbFileName = wb.getThumbFileName();
            this.description = wb.getDescription();
            this.created = wb.getCreated();
            this.updated = wb.getUpdated();
            this.guid = wb.getGuid();
        }

        public WhiteBoardBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public WhiteBoardBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public WhiteBoardBuilder setImageFileName(String imageFileName) {
            this.imageFileName = imageFileName;
            return this;
        }

        public WhiteBoardBuilder setThumbFileName(String thumbFileName) {
            this.thumbFileName = thumbFileName;
            return this;
        }

        public WhiteBoardBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public WhiteBoardBuilder setCreated(long created) {
            this.created = created;
            return this;
        }

        public WhiteBoardBuilder setUpdated(long updated) {
            this.updated = updated;
            return this;
        }

        public WhiteBoardBuilder setGuid(String guid) {
            this.guid = guid;
            return this;
        }

        public Whiteboard build() {
            return new Whiteboard(id, title, imageFileName,thumbFileName, description, created, updated, guid);
        }
    }
}
