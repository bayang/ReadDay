package me.bayang.reader.components;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfoenix.controls.JFXListCell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.bayang.reader.backend.inoreader.ConnectServer;
import me.bayang.reader.backend.inoreader.FolderFeedOrder;
import me.bayang.reader.controllers.RssController;
import me.bayang.reader.rssmodels.Item;
import me.bayang.reader.utils.StringUtils;

public class ItemListCell extends JFXListCell<Item> {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    
    @FXML
    private VBox cellWrapper;
    
    @FXML
    private HBox firstLine;
    
    @FXML
    private Label fromLabel;

    @FXML
    private Label dateLabel;
    
    @FXML
    private Label subjectLabel;

    @FXML
    private Label contentLabel;
    
    @FXML
    private ImageView icon;
    
    private FXMLLoader mLLoader;
    
    private ContextMenu menu = new ContextMenu();
    
    private ConnectServer connectServer;
    
    private RssController rssController;

    public ItemListCell(RssController rssController, ConnectServer connectServer) {
        this.connectServer = connectServer;
        this.rssController = rssController;
        MenuItem starItem = new MenuItem("Mark Starred");
        MenuItem unStarItem = new MenuItem("Mark Unstarred");
        menu.getItems().addAll(starItem, unStarItem);

        starItem.setOnAction(event -> {
            LOGGER.debug("mark star " + getListView().getSelectionModel().getSelectedItem().getDecimalId());
            this.connectServer.star(this.getListView().getSelectionModel().getSelectedItem().getDecimalId());
            this.rssController.addToStarredList(this.getListView().getSelectionModel().getSelectedItem());
        });
        unStarItem.setOnAction(event -> {
            LOGGER.debug("unstar " + this.getListView().getSelectionModel().getSelectedItem().getDecimalId());
            this.connectServer.unStar(this.getListView().getSelectionModel().getSelectedItem().getDecimalId());
        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                e.consume();
            }
        });
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        this.prefWidthProperty().bind( this.getListView().widthProperty().subtract( 20 ) );
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(
                        getClass().getResource("/fxml/ItemListCell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    LOGGER.error("",e);
                }
            }
            
            //get the time style
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(item.getCrawlTimeMsec())), ZoneId.systemDefault());
            String timeString = localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("kk:mm:ss"));
            if (!localDateTime.toLocalDate().equals(LocalDate.now())) {
                timeString = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            }
            fromLabel.setTextFill(item.isRead() ? Color.GRAY : Color.BLACK);
            fromLabel.setText(StringEscapeUtils.unescapeHtml4(item.getOrigin().getTitle()));
            dateLabel.setTextFill(item.isRead() ? Color.GRAY : Color.BLACK);
            dateLabel.setText(timeString);
            subjectLabel.setStyle("-fx-font-weight: bold;");
            subjectLabel.setTextFill(item.isRead() ? Color.GRAY : Color.BLACK);
            subjectLabel.setWrapText(true);
            subjectLabel.setText(StringEscapeUtils.unescapeHtml4(item.getTitle()));
            contentLabel.setText(StringUtils.processContent(item.getSummary().getContent()));
            
            setText(null);
            setTextFill(item.isRead() ? Color.GRAY : Color.BLACK);
            if (FolderFeedOrder.iconMap != null) {
                icon.setImage(FolderFeedOrder.iconMap.get(item.getOrigin().getStreamId()));
                icon.setFitWidth(20);
                icon.setFitHeight(20);
            }
            setGraphic(cellWrapper);
            setContextMenu(menu);
        }
    }

}