package com.example.lab6_and103_ph51025.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lab6_and103_ph51025.databinding.ActivityUpdateFruitBinding;
import com.example.lab6_and103_ph51025.model.Distributor;
import com.example.lab6_and103_ph51025.services.HttpRequest;
import com.example.lab6_and103_ph51025.R;
import com.example.lab6_and103_ph51025.model.Response;
import com.example.lab6_and103_ph51025.model.Fruit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class UpdateFruitActivity extends AppCompatActivity {
    ActivityUpdateFruitBinding binding;
    private Fruit fruit;
    private String id ;
    private HttpRequest httpRequest;
    private String id_Distributor;
    private ArrayList<Distributor> distributorArrayList;
    private ArrayList<File> ds_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUpdateFruitBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        ds_image = new ArrayList<>();
        httpRequest = new HttpRequest();


        getDataIntent();
        userListener();
        configSpinner();



    }

    private void getDataIntent() {
        // Nhận đối tượng Fruit từ Intent
        Intent intent = getIntent();
        fruit = (Fruit) intent.getSerializableExtra("fruit");

        if (fruit == null) {
            Log.e("getDataIntent", "Fruit object is null");
            Toast.makeText(this, "No fruit data received", Toast.LENGTH_SHORT).show();
            return; // Kết thúc nếu fruit null
        }

        // Kiểm tra danh sách ảnh
        if (fruit.getImage() != null && !fruit.getImage().isEmpty()) {
            // Lấy URL ảnh đầu tiên
            String url = fruit.getImage().get(0);
            String newUrl = url.replace("localhost", "10.0.2.2");

            // Sử dụng Glide để tải ảnh
            Glide.with(this)
                    .load(newUrl)
                    .thumbnail(Glide.with(this).load(R.drawable.baseline_broken_image_24))
                    .error(R.drawable.baseline_broken_image_24) // Hiển thị ảnh lỗi nếu không tải được
                    .into(binding.avatar);
        } else {
            Log.e("getDataIntent", "Image list is null or empty");
            Toast.makeText(this, "No images available", Toast.LENGTH_SHORT).show();
        }

        // Cập nhật các trường thông tin khác
        if (fruit.getName() != null) {
            binding.edName.setText(fruit.getName());
        }

        binding.edPrice.setText(String.valueOf(fruit.getPrice())); // Đảm bảo kiểu dữ liệu là String
        binding.edQuantity.setText(String.valueOf(fruit.getQuantity()));
        if (fruit.getDescription() != null) {
            binding.edDescription.setText(fruit.getDescription());
        }
        if (fruit.getStatus() != null) {
            binding.edStatus.setText(fruit.getStatus());
        }
    }


    private RequestBody getRequestBody(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"),value);
    }

    private void userListener() {
        binding.avatar.setOnClickListener(v -> chooseImage());

        binding.btnUpdate.setOnClickListener(v -> {
            Map<String, RequestBody> mapRequestBody = new HashMap<>();
            String _name = binding.edName.getText().toString().trim();
            String _quantity = binding.edQuantity.getText().toString().trim();
            String _price = binding.edPrice.getText().toString().trim();
            String _status = binding.edStatus.getText().toString().trim();
            String _description = binding.edDescription.getText().toString().trim();

            mapRequestBody.put("name", getRequestBody(_name));
            mapRequestBody.put("quantity", getRequestBody(_quantity));
            mapRequestBody.put("price", getRequestBody(_price));
            mapRequestBody.put("status", getRequestBody(_status));
            mapRequestBody.put("description", getRequestBody(_description));
            mapRequestBody.put("id_distributor", getRequestBody(id_Distributor));

            // Tạo danh sách MultipartBody cho ảnh
            ArrayList<MultipartBody.Part> _ds_image = new ArrayList<>();

            // Kiểm tra ảnh mới
            if (!ds_image.isEmpty()) {
                // Thêm ảnh mới vào danh sách
                ds_image.forEach(file -> {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                    _ds_image.add(multipartBodyPart);
                });
                Log.d("UpdateFruit", "Đã chọn ảnh mới");
            } else {
                // Nếu không có ảnh mới, giữ lại ảnh cũ
                for (String imagePath : fruit.getImage()) {
                    File imageFile = new File(imagePath.replace("localhost", "10.0.2.2"));
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
                    MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
                    _ds_image.add(multipartBodyPart);
                }
                Log.d("UpdateFruit", "Sử dụng ảnh cũ");
            }

            // Gửi yêu cầu cập nhật lên server
            httpRequest.callAPI()
                    .updateFruitWithFileImage(mapRequestBody, fruit.get_id(), _ds_image)
                    .enqueue(responseFruit);
        });
    }

    Callback<Response<Fruit>> responseFruit = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful()) {
                Log.d("123123", "onResponse: " + response.body().getStatus());
                if (response.body().getStatus()==200) {
                    Toast.makeText(UpdateFruitActivity.this, "Sửa thành công thành công", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        }

        @Override
        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
            Toast.makeText(UpdateFruitActivity.this, "Sửa sai rôi thằng ngu ", Toast.LENGTH_SHORT).show();
            onBackPressed();
            Log.e("zzzzzzzzzz", "onFailure: "+t.getMessage());
        }
    };


    private void chooseImage() {
//        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        Log.d("123123", "chooseAvatar: " +123123);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        getImage.launch(intent);
//        }else {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//
//        }
    }

    ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        ds_image.clear();
                        Intent data = o.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();

                                File file = createFileFormUri(imageUri, "image" + i);
                                ds_image.add(file);
                            }


                        } else if (data.getData() != null) {
                            // Trường hợp chỉ chọn một hình ảnh
                            Uri imageUri = data.getData();
                            // Thực hiện các xử lý với imageUri
                            File file = createFileFormUri(imageUri, "image" );
                            ds_image.add(file);

                        }
                        Glide.with(UpdateFruitActivity.this)
                                .load(ds_image.get(0))
                                .thumbnail(Glide.with(UpdateFruitActivity.this).load(R.drawable.baseline_broken_image_24))
                                .centerCrop()
                                .circleCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(binding.avatar);
                    }
                }
            });

    private File createFileFormUri (Uri path, String name) {
        File _file = new File(UpdateFruitActivity.this.getCacheDir(), name + ".png");
        try {
            InputStream in = UpdateFruitActivity.this.getContentResolver().openInputStream(path);
            OutputStream out = new FileOutputStream(_file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) >0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            Log.d("123123", "createFileFormUri: " +_file);
            return _file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    private void configSpinner() {
        httpRequest.callAPI().getListDistributor().enqueue(getDistributorAPI);
        binding.spDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                id_Distributor = distributorArrayList.get(position).getId();
                Log.d("123123", "onItemSelected: " + id_Distributor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.spDistributor.setSelection(0);
    }

    Callback<Response<ArrayList<Distributor>>> getDistributorAPI = new Callback<Response<ArrayList<Distributor>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Distributor>>> call, retrofit2.Response<Response<ArrayList<Distributor>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    distributorArrayList = response.body().getData();
                    String[] items = new String[distributorArrayList.size()];

                    for (int i = 0; i< distributorArrayList.size(); i++) {
                        items[i] = distributorArrayList.get(i).getName();
                    }
                    ArrayAdapter<String> adapterSpin = new ArrayAdapter<>(UpdateFruitActivity.this, android.R.layout.simple_spinner_item, items);
                    adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spDistributor.setAdapter(adapterSpin);
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Distributor>>> call, Throwable t) {
            t.getMessage();
        }

    };


}