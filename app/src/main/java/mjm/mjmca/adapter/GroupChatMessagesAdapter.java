package mjm.mjmca.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.BuildConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;


import mjm.mjmca.R;
import mjm.mjmca.activities.ImageViewActivity;
import mjm.mjmca.model.ChatMessages;
import mjm.mjmca.model.GroupMessages;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class GroupChatMessagesAdapter extends RecyclerView.Adapter<GroupChatMessagesAdapter.ViewHolder> {

    private boolean playPause;
    private boolean initialStage = true;
    private MediaPlayer mediaPlayer;
    public static final int MESSAGE_LEFT = 0;
    public static final int MESSAGE_RIGHT = 1;
    Context context;
    List<GroupMessages> chatMessages;
    DatabaseReference reference;

    FirebaseUser firebaseUser;
    public GroupChatMessagesAdapter(Context context, List<GroupMessages> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        reference = FirebaseDatabase.getInstance().getReference();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (viewType == MESSAGE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_sender, parent, false);
            return new ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_receiver, parent, false);
            return new ViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String senderId = firebaseUser.getUid();
        final GroupMessages cm = chatMessages.get(position);
        String time = cm.getTime();
        String fromMEssageType = cm.getType();
        String fromUSerID = cm.getSender();
        if (fromMEssageType.equals("document")){
            holder.chat_documents.setVisibility(View.VISIBLE);
            setUsername(cm, holder);
            holder.chat_videos.setVisibility(View.GONE);
            holder.chat_audios.setVisibility(View.GONE);
            holder.chat_images.setVisibility(View.GONE);
            holder.chat_message.setVisibility(View.GONE);
            String size = cm.getFileSize();
            String sizes = finalSize(Integer.parseInt(size));
            /*String sizeB = cm.getFileSize();
            int sizeKB = Integer.parseInt(sizeB) / 1024;
            int sizeMB = sizeKB / 1024;
            String finalSize = String.valueOf(sizeMB);*/
            holder.fileExtension.setText(sizes);
            /*if (cm.isIsseen()){
                holder.delivery_seen_of_document.setImageResource(R.mipmap.seen);
            }
            else{
                holder.delivery_seen_of_document.setImageResource(R.mipmap.delivered);
            }*/
            holder.download_document.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.download_document.setVisibility(View.GONE);
                    final DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(cm.getDownloadURl());
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, cm.getFileName());
                    if (downloadManager != null) {
                        downloadManager.enqueue(request);
                    }
                    Toast.makeText(context, "When download is done you will be notified", Toast.LENGTH_SHORT).show();


                }
            });
            if (cm.getFileExtension().equals("pdf")){
                holder.chat_documents.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + cm.getFileName());
                        Uri apkURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                        /*Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);*/
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setDataAndType(apkURI, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        }
                        Intent intent1 = Intent.createChooser(intent, "Open File with:");
                        try{
                            context.startActivity(intent1);
                        }catch (ActivityNotFoundException e){
                            e.printStackTrace();
                            Toast.makeText(context, "No application available to open file", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if (cm.getFileExtension().equals("apk")){
                holder.chat_documents.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + cm.getFileName());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        context.startActivity(intent);*/
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + cm.getFileName());
                        Intent intent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            Uri apkUri = Uri.fromFile(file);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(intent);
                    }
                });
            }
            holder.chat_documents.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
        if (fromMEssageType.equals("audio")){
            holder.chat_audios.setVisibility(View.VISIBLE);
            holder.chat_documents.setVisibility(View.GONE);
            setUsername(cm, holder);
            holder.chat_videos.setVisibility(View.GONE);
            holder.chat_images.setVisibility(View.GONE);
            holder.chat_message.setVisibility(View.GONE);
            final String audioUrl = cm.getDownloadURl();
            /*holder.cardView_of_audio.setVisibility(View.VISIBLE);
            holder.date_time_ofaudio.setVisibility(View.VISIBLE);
            holder.date_time_ofaudio.setText(time);*/
            holder.audio_name.setText(cm.getFileName());
            /*if (cm.isIsseen()){
                holder.delivery_Seen_of_audio.setImageResource(R.mipmap.seen);
            }
            else{
                holder.delivery_Seen_of_audio.setImageResource(R.mipmap.delivered);
            }*/
            holder.chat_audios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, mjm.mjmca.activities.audio_streaming.class);
                    intent.putExtra("audioUrl", audioUrl);
                    intent.putExtra("audioName", cm.getFileName());
                    context.startActivity(intent);
                }
            });
            holder.chat_audios.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, "Message Deleted", Toast.LENGTH_LONG);
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
        if (fromMEssageType.equals("video")){
            holder.chat_audios.setVisibility(View.GONE);
            holder.chat_documents.setVisibility(View.GONE);
            setUsername(cm, holder);
            holder.chat_videos.setVisibility(View.VISIBLE);
            holder.chat_images.setVisibility(View.GONE);
            holder.chat_message.setVisibility(View.GONE);
            holder.chat_videos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, mjm.mjmca.activities.videoStreaming.class);
                    intent.putExtra("videourl", cm.getDownloadURl());
                    context.startActivity(intent);
                }
            });
            /*if (cm.isIsseen()){
                holder.delivery_Seen_of_video.setImageResource(R.mipmap.seen);
            }
            else{
                holder.delivery_Seen_of_video.setImageResource(R.mipmap.delivered);
            }*/
            holder.chat_videos.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
        if (fromMEssageType.equals("image")){
            holder.chat_audios.setVisibility(View.GONE);
            holder.chat_documents.setVisibility(View.GONE);
            holder.chat_videos.setVisibility(View.GONE);
            setUsername(cm, holder);
            holder.chat_images.setVisibility(View.VISIBLE);
            holder.chat_message.setVisibility(View.GONE);
            Picasso.get().load(cm.getDownloadURl()).into(holder.chat_images);
            /*if (cm.isIsseen()){
                holder.deliver_seen_of_image.setImageResource(R.mipmap.seen);
            }
            else{
                holder.deliver_seen_of_image.setImageResource(R.mipmap.delivered);
            }*/
            holder.chat_images.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("imageUrl", cm.getDownloadURl());
                    context.startActivity(intent);
                }
            });
            holder.chat_images.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
        if (fromMEssageType.equals("camera")){
            holder.chat_audios.setVisibility(View.GONE);
            holder.chat_documents.setVisibility(View.GONE);
            holder.chat_videos.setVisibility(View.GONE);
            setUsername(cm, holder);
            holder.chat_images.setVisibility(View.VISIBLE);
            holder.chat_message.setVisibility(View.GONE);
            Picasso.get().load(cm.getDownloadURl()).into(holder.chat_images);
            /*if (cm.isIsseen()){
                holder.deliver_seen_of_image.setImageResource(R.mipmap.seen);
            }
            else{
                holder.deliver_seen_of_image.setImageResource(R.mipmap.delivered);
            }*/if (!cm.isIsseen()){
                holder.chat_message.setBackgroundResource(R.drawable.seen);
            }
            holder.chat_images.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("imageUrl", cm.getDownloadURl());
                    context.startActivity(intent);
                }
            });
            holder.chat_images.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
        if (fromMEssageType.equals("text")){
            holder.chat_audios.setVisibility(View.GONE);
            holder.chat_documents.setVisibility(View.GONE);
            holder.chat_videos.setVisibility(View.GONE);
            holder.chat_images.setVisibility(View.GONE);
            holder.chat_message.setVisibility(View.VISIBLE);
            holder.chat_message.setText(cm.getMessage());
            setUsername(cm, holder);
            if (!cm.isIsseen()){
                holder.chat_message.setBackgroundResource(R.drawable.seen);
            }
            /*if (cm.isIsseen()){
             *//*holder.seen_delivered.setImageResource(R.mipmap.seen);*//*
                holder.messages.setBackgroundResource(R.drawable.seen);
            }
            else{
                *//*holder.seen_delivered.setImageResource(R.mipmap.delivered);*//*
                holder.messages.setBackgroundResource(R.drawable.receiver);
            }*/
            holder.chat_message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose");
                    builder.setMessage("Are you going to delete messages?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference.child(cm.getGroupId()).child("Messages").child(cm.getMessagePushID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }

        /*if (position == chatMessages.size() -1){
            if (cm.isIsseen()){
                holder.seen_delivered.setImageResource(R.mipmap.seen);
            }
            else{
                holder.seen_delivered.setImageResource(R.mipmap.delivered);
            }
        }else{
            holder.seen_delivered.setVisibility(View.VISIBLE);
        }*/


    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView chat_message, fileExtension, audio_name, name;
        public ImageView chat_images, chat_videos, download_document;
        public CardView chat_audios, chat_documents;
        /*public ProgressBar progressBar;
        public CardView cardView_of_image, cardView_of_video, cardView_of_audio, cardView_of_document;
        public TextView fileExtension, messages, date_time, date_time_of_image, date_time_of_video, date_time_ofaudio, audio_name, document_name, date_timeof_document;
        public ImageView seen_delivered, images, deliver_seen_of_image, delivery_Seen_of_video, delivery_Seen_of_audio, delivery_seen_of_document, download_document;
        public View video_view;
        public RelativeLayout message_relativeLayout, images_relativeLayout, videos_relativeLayout, audios_relativeLayout, documents_relativeLayout;*/
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.messageN);
            chat_documents = itemView.findViewById(R.id.chat_documents);
            chat_message = itemView.findViewById(R.id.chatMessages);
            chat_images = itemView.findViewById(R.id.chat_images);
            chat_audios = itemView.findViewById(R.id.chat_audios);
            chat_videos = itemView.findViewById(R.id.chat_videos);
            fileExtension = itemView.findViewById(R.id.fileExtension);
            download_document = itemView.findViewById(R.id.download_document);
            audio_name = itemView.findViewById(R.id.audio_name);

            /*cardView_of_image = itemView.findViewById(R.id.cardView_of_image);
            messages = itemView.findViewById(R.id.chat_messages);
            seen_delivered = itemView.findViewById(R.id.delivered_seen);
            date_time = itemView.findViewById(R.id.date_time);
            images = itemView.findViewById(R.id.images);
            date_time_of_image = itemView.findViewById(R.id.date_time_of_image);
            deliver_seen_of_image = itemView.findViewById(R.id.delivered_seen_of_image);
            date_time_of_video = itemView.findViewById(R.id.date_time_of_video);
            delivery_Seen_of_video = itemView.findViewById(R.id.delivered_seen_of_video);
            cardView_of_video = itemView.findViewById(R.id.cardView_of_video);
            video_view = itemView.findViewById(R.id.play_video);
            delivery_Seen_of_audio = itemView.findViewById(R.id.delivered_seen_of_audio);
            date_time_ofaudio = itemView.findViewById(R.id.date_time_of_audio);
            cardView_of_audio = itemView.findViewById(R.id.cardView_of_audio);
            audio_name = itemView.findViewById(R.id.audio_name);
            document_name = itemView.findViewById(R.id.document_name);
            date_timeof_document = itemView.findViewById(R.id.date_time_of_document);
            delivery_seen_of_document = itemView.findViewById(R.id.delivered_seen_of_document);
            download_document = itemView.findViewById(R.id.download_document);
            cardView_of_document = itemView.findViewById(R.id.cardView_of_document);
            message_relativeLayout = itemView.findViewById(R.id.message_relativeLayout);
            images_relativeLayout = itemView.findViewById(R.id.images_relativeLayout);
            videos_relativeLayout = itemView.findViewById(R.id.video_relativeLayout);
            audios_relativeLayout = itemView.findViewById(R.id.audio_relativeLayout);
            documents_relativeLayout = itemView.findViewById(R.id.document_relativeLayout);
            progressBar = itemView.findViewById(R.id.progress);
            fileExtension = itemView.findViewById(R.id.fileExtension);*/
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatMessages.get(position).getSender().equals(firebaseUser.getUid())){
            return MESSAGE_RIGHT;
        }
        else{
            return MESSAGE_LEFT;
        }
    }

    public void downloadFile(Context contexts,String filename, String fileExtension, String destinationDirectory, String url ){
        final DownloadManager downloadManager = (DownloadManager)contexts.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(contexts, destinationDirectory, filename + fileExtension);
        downloadManager.enqueue(request);
        final long downLoadId = downloadManager.enqueue(request);
        Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downLoadId));
        if (cursor!=null && cursor.moveToNext()){
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            cursor.close();
            if (status == DownloadManager.STATUS_SUCCESSFUL){

            }
        }
    }

    private void setUsername(GroupMessages groupMessages, final ViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        databaseReference.orderByChild("uid").equalTo(groupMessages.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    holder.name.setText(name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public static String finalSize(int bytes){
        if (-1000<bytes&&bytes<1000){
            return bytes + "B";
        }
        CharacterIterator characterIterator = new StringCharacterIterator("KMGTPE");
        while (bytes<=-999_950||bytes>999_950){
            bytes/=1000;
            characterIterator.next();
        }
        return String.format("%.1f%cB", bytes/1000.0,characterIterator.current());
    }
    class Player extends AsyncTask<String, Void, Boolean>{


        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean prepared;
            try{
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        initialStage = true;
                        playPause = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            }catch (IllegalArgumentException e){
                prepared = false;
                e.printStackTrace();
            }catch (SecurityException e){
                prepared = false;
                e.printStackTrace();
            }catch (IOException e){
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            mediaPlayer.start();
            initialStage = false;
        }

        Player(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
    }

}


