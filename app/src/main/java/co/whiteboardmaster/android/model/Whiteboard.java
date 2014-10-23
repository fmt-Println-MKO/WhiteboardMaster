package co.whiteboardmaster.android.model;

import java.io.Serializable;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class Whiteboard implements Serializable{

    private int id;
    private String title;
    private String path;
    private String description;
    private long created;
    private long updated;
    private String guid;


    public Whiteboard(int id, String title, String path, String description, long created, long updated, String guid) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.description = description;
        this.created = created;
        this.updated = updated;
        this.guid = guid;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
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
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", guid='" + guid + '\'' +
                '}';
    }

    public static class WhiteBoardBuilder {

        private int id;
        private String title;
        private String path;
        private String description;
        private long created;
        private long updated;
        private String guid;


        public WhiteBoardBuilder() {
        }
        public WhiteBoardBuilder(Whiteboard wb) {
            this.id = wb.getId();
            this.title = wb.getTitle();
            this.path = wb.getPath();
            this.description = wb.getDescription();
            this.created = wb.getCreated();
            this.updated = wb.getUpdated();
            this.guid = wb.getGuid();
        }

        public WhiteBoardBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public WhiteBoardBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public WhiteBoardBuilder setPath(String path) {
            this.path = path;
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
            return new Whiteboard(id, title, path, description, created, updated, guid);
        }
    }
}
